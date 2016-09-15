package nablarch.test.support.reader;

import java.util.ArrayList;
import java.util.List;

import nablarch.fw.DataReader;
import nablarch.fw.ExecutionContext;

/**
 * 既存のデータリーダに対して、複数件データのまとめ読み機能を追加するクラス。
 * <pre>
 * 既存のデータリーダから読み込んだデータを内部的にバッファリングし、
 * 一定の条件が満たされた時点でまとめて返却する。
 * </pre>
 * @param <D> 元となるデータリーダのデータ型
 * @author Iwauo Tajima
 */
public abstract class AggregatingDataReader<D> implements DataReader<List<D>> {
    
    /** 元となるデータリーダ */
    private DataReader<D> sourceDataReader;
      
    /** データを保持するバッファの初期サイズ */
    private int bufferSize = 10;
    
    /** 同一グループに所属するデータを保持するバッファ */
    private List<D> dataBuffer = null;

    /** 
     * データを保持するバッファの初期サイズを設定する。
     * @param bufferSize バッファの初期サイズ
     * @return このオブジェクト自体
     */
    public synchronized AggregatingDataReader<D> setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
        return this;
    }
    
    /**
     * 元となるデータリーダからデータを読み込み、
     * 一定の条件が満たされた時点でまとめて返却する。
     * @param ctx {@inheritDoc}
     * @return 入力データオブジェクトのリスト
     */
    public List<D> read(ExecutionContext ctx) {
        if (getSourceDataReader() == null) {
            throw new IllegalArgumentException(
                    "source data reader was not set. source data reader must be set before reading.");
        }
        List<D> result = null;
        D newData = null;
        synchronized (this) {
            if (bufferSize < 0) {
                throw new IllegalArgumentException("buffer size was invalid. buffer size must be bigger than 0.");
            }
            while (hasNext(ctx)) {
                newData = getSourceDataReader().read(ctx);
                if (newData == null) {
                    // エンドレコードに到達した場合、バッファを結果として返却し、ループを抜ける
                    handleEnd(dataBuffer, ctx);
                    result = dataBuffer;
                    dataBuffer = null;
                    break;
                }
                if (dataBuffer == null) {
                    dataBuffer = new ArrayList<D>(bufferSize);
                }
                if (dataBuffer.isEmpty() || isInSameGroup(newData, dataBuffer, ctx)) {
                    dataBuffer.add(newData);
                    continue;
                } else {
                    // 前に読み込んだレコードと、新しく読み込んだレコードが同一グループでない場合、ループを抜ける
                    result = dataBuffer;
                    dataBuffer = new ArrayList<D>(bufferSize);
                    dataBuffer.add(newData);
                    break;
                }
            }
        }
        return result;
    }

    /**
     * エンドレコードに到達した場合に処理を実行する。
     * <p>
     * 本クラスの実装では何も処理を行わない。
     * 必要に応じてサブクラスでオーバーライドすること。
     * </p>
     * @param dataBuffer バッファリングしているデータのリスト
     * @param ctx 実行コンテキスト
     */
    protected void handleEnd(List<D> dataBuffer, ExecutionContext ctx) {
        // NOP
    }

    /**
     * 元となるデータリーダの次に読み込むデータが存在するかどうかを返却する。
     * @param ctx {@inheritDoc}
     * @return 元となるデータリーダの次に読み込むデータがまだ残っている場合はtrue
     */
    public synchronized boolean hasNext(ExecutionContext ctx) {
        if (dataBuffer != null && !dataBuffer.isEmpty()) {
            return true;
        }
        return getSourceDataReader().hasNext(ctx);
    }
    
    /** {@inheritDoc}
     * この実装では、内部的に保持しているリーダを閉じる。
     */
    public synchronized void close(ExecutionContext ctx) {
        if (getSourceDataReader() == null) {
            return;
        }
        getSourceDataReader().close(ctx);
    }

    /**
     * 元となるデータリーダを返却する。
     * @return 元となるデータリーダ
     */
    public DataReader<D> getSourceDataReader() {
        return sourceDataReader;
    }
    
    /**
     * 元となるデータリーダを設定する。
     * @param source 元となるデータリーダ
     * @return このオブジェクト自体
     */
    @SuppressWarnings({ "unchecked" })
    public AggregatingDataReader<D> setSourceDataReader(DataReader<? extends D> source) {
        sourceDataReader = (DataReader<D>) source;
        return this;
    }

    /**
     * 新しく読み込んだデータが、バッファリングしているデータのグループに所属するかどうかを返却する。
     * 
     * @param newData 新たに読み込んだデータ
     * @param dataBuffer バッファリングしているデータのリスト
     * @param ctx 実行コンテキスト
     * @return 新しく読み込んだデータが、バッファリングしているデータのグループに所属する場合はtrue
     */
    protected abstract boolean isInSameGroup(D newData, List<D> dataBuffer,
            ExecutionContext ctx);
    
}
