package nablarch.test.support.reader;

import java.util.List;
import java.util.Map;

import nablarch.core.log.Logger;
import nablarch.core.log.LoggerManager;
import nablarch.fw.ExecutionContext;


/**
 * 既存のデータリーダに対しコントロールブレイク機能を追加するデータリーダ。
 * <p>
 * データリーダが読み込むデータの特定項目の値を監視し、
 * 前回の値から変更があるまでデータを内部的にバッファリングする。
 * 変更が発生した時点でバッファリングしていたデータをまとめて返す。
 * </p>
 * <p>
 * 本クラスのパラメータ<D>には、元となるデータリーダのデータの型を指定すること。
 * </p>
 * <p>
 * 実装例を以下に示す。
 * <pre>
 * {@code
 * DatabaseRecordReader dbReader = new DatabaseRecordReader();
 * ControlBreakReader<SqlRow> reader = new ControlBreakReader<SqlRow>(); // ControlBreakReaderクラスのパラメータ<D>に、DatabaseRecordReaderのデータの型<SqlRow>を指定する
 * reader.setSourceDataReader(dbReader);
 * }
 * </pre>
 * </p>
 * @param <D> 元となるデータリーダのデータの型
 * @author Iwauo Tajima
 */
public class ControlBreakReader<D extends Map<String, Object>>
extends AggregatingDataReader<D> {
    
    /** Logger */
    private static final Logger LOGGER = LoggerManager.get(
            ControlBreakReader.class);

    /** リクエストスコープに設定するキーのPrefix */
    protected static final String REQUEST_SCOPE_KEY_PREFIX = ExecutionContext.FW_PREFIX + "ControlBreakReader_";
    
    /** コントロールブレイクが発生したキーを、リクエストスコープに格納するキー */
    private static final String BREAK_KEY = REQUEST_SCOPE_KEY_PREFIX + "breakKey";

    /** ブレイクキーに対応する値（ブレイクする前の値）を、リクエストスコープから取得するためのキー */
    private static final String PREVIOUS_VALUE = REQUEST_SCOPE_KEY_PREFIX + "previousValue";

    /** 値の変更を監視するキー */
    private String[] watchedKeys = null;
    
    /**
     * {@inheritDoc}
     * <p>
     * この実装では、監視対象キーの値に変化があった時点
     * (=コントロールブレイクが発生した時点)でfalseを返す。
     * </p>
     * @return 監視対象キーの値が変化した場合はfalse
     */
    @Override
    protected boolean isInSameGroup(D newData, List<D> dataBuffer,
            ExecutionContext ctx) {
        D prevData = dataBuffer.get(dataBuffer.size() - 1);

        for (String watchedKey : watchedKeys) {
            Object prevValue = prevData.get(watchedKey);
            Object newValue = newData.get(watchedKey);
            if (prevValue == null) {
                if (newValue != null) {
                    handleBreak(watchedKey, ctx, prevData, newData);
                    return false;
                }
            } else {
                if (!prevValue.equals(newValue)) {
                    handleBreak(watchedKey, ctx, prevData, newData);
                    return false;
                }
            }
        }
        return true;
    }

    
    /**
     * コントロールブレイク発生時のブレイクキーおよび、ブレイクキーに対応する値（ブレイクする前の値）を、リクエストスコープに設定する。
     * @param breakKey ブレイクキー
     * @param ctx 実行コンテキスト
     * @param prevData 1つ前に読み込んだデータ
     * @param newData 新たに読み込んだデータ
     */
    protected void handleBreak(String breakKey, ExecutionContext ctx,
            Map<String, Object> prevData, Map<String, Object> newData) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.logDebug(String
                    .format("control break occurred. key=[%s], previous value=[%s], new value=[%s]",
                            breakKey, prevData.get(breakKey), newData.get(
                            breakKey)));
        }
        ctx.setRequestScopedVar(BREAK_KEY, breakKey);
        ctx.setRequestScopedVar(PREVIOUS_VALUE, prevData.get(breakKey));
    }

    /**
     * エンドレコードに到達した場合、監視キーおよび値をリクエストスコープへ設定する。
     * 監視キーが複数ある場合は、配列の最初に設定された監視キーに対応する値を設定する。
     * @param dataBuffer {@inheritDoc}
     * @param ctx {@inheritDoc}
     */
    @Override
    protected void handleEnd(List<D> dataBuffer, ExecutionContext ctx) {
        D prevData = dataBuffer.get(dataBuffer.size() - 1);
        ctx.setRequestScopedVar(PREVIOUS_VALUE, prevData.get(watchedKeys[0]));
    }
    
    /**
     * 値の変更を監視するキーを設定する。<br/>
     * キーは複数設定することができ、設定した順番で値の変更が監視される。<br/>
     * 使用例を以下に示す。
     * <pre>
     * ControlBreakReader<SqlRow> reader = new ControlBreakReader<SqlRow>();
     * reader.setWatchedKeys("userId", "itemId"); // ユーザID、商品IDの順番で値の変更を監視する。
     * </pre>
     * @param watchedKeys 値の変更を監視するキーのリスト
     * @return このオブジェクト自体
     */
    public ControlBreakReader<D> setWatchedKeys(String... watchedKeys) {
        this.watchedKeys = watchedKeys;
        return this;
    }
    
    /**
     * コントロールブレイクが発生したキーを取得する。
     * @param ctx 実行コンテキスト
     * @return コントロールブレイクが発生したキー
     */
    public static String getControlBreakKey(ExecutionContext ctx) {
        return ctx.getRequestScopedVar(BREAK_KEY);
    }
    
    /**
     * ブレイクキーに対応する値（ブレイクする前の値）を取得する。
     * @param ctx 実行コンテキスト
     * @return ブレイクキーに対応する値（ブレイクする前の値）
     */
    public static Object getPreviousValue(ExecutionContext ctx) {
        return ctx.getRequestScopedVar(PREVIOUS_VALUE);
    }
}
