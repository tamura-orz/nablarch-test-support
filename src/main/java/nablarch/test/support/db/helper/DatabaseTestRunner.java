package nablarch.test.support.db.helper;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Properties;

import nablarch.core.db.connection.AppDbConnection;
import nablarch.core.db.connection.DbConnectionContext;
import nablarch.core.db.connection.TransactionManagerConnection;
import nablarch.core.repository.di.DiContainer;
import nablarch.core.repository.di.config.xml.XmlComponentDefinitionLoader;
import nablarch.core.transaction.TransactionContext;
import nablarch.core.util.FileUtil;

import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

/**
 * データベース関連のテストを実行するためのJunitTestRunner。
 *
 * このクラスを{@link org.junit.runner.RunWith}に指定することで、
 * テスト用のデータベース接続関連の設定が自動的に行われる。
 *
 * これにより、テストクラス内部では、{@link VariousDbTestHelper}を使用してデータベースへのアクセスが行えるようになる。
 */
public class DatabaseTestRunner extends BlockJUnit4ClassRunner {

    public DatabaseTestRunner(Class<?> klass) throws InitializationError {
        super(klass);
    }

    @Override
    public void run(RunNotifier notifier) {
        final Description description = getDescription();
        final TargetDb annotation = findTargetDbAnnotation();
        if (!isRun(annotation)) {
            notifier.fireTestIgnored(description);
            return;
        }
        final DiContainer container = new DiContainer(new XmlComponentDefinitionLoader("db-default.xml"));
        VariousDbTestHelper.initialize(container);
        super.run(notifier);
    }

    @Override
    protected void runChild(FrameworkMethod method, RunNotifier notifier) {
        final TargetDb annotation = method.getAnnotation(TargetDb.class);
        Description description= describeChild(method);
        if (!isRun(annotation)) {
            notifier.fireTestIgnored(description);
            return;
        }
        try {
            super.runChild(method, notifier);
        } finally {
            clearDatabaseResource();
        }
    }

    private void clearDatabaseResource() {
        try {
            final Field field = DbConnectionContext.class.getDeclaredField("connection");
            field.setAccessible(true);
            final ThreadLocal<Map<String, AppDbConnection>> connectionList = (ThreadLocal<Map<String, AppDbConnection>>) field.get(null);
            for (Map.Entry<String, AppDbConnection> entry : connectionList.get().entrySet()) {
                final String key = entry.getKey();
                final TransactionManagerConnection connection = DbConnectionContext.getTransactionManagerConnection(
                        key);
                try {
                    connection.terminate();
                } catch (Exception ignore) {
                }
                DbConnectionContext.removeConnection(key);
                TransactionContext.removeTransaction(key);
            }
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isRun(TargetDb annotation) {
        if (annotation == null) {
            return true;
        }
        final String url = getUrl();
        if (url == null) {
            throw new IllegalStateException("database configuration file was not found.");
        }

        final TargetDb.Db[] exclude = annotation.exclude();
        final TargetDb.Db[] include = annotation.include();
        if (exclude.length != 0) {
            for (TargetDb.Db db : exclude) {
                if (url.startsWith(db.prefix)) {
                    return false;
                }
            }
            return true;
        } else if (include.length != 0) {
            for (TargetDb.Db db : include) {
                if (url.startsWith(db.prefix)) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }

    private String getUrl() {
        Properties properties = new Properties();
        InputStream stream = FileUtil.getClasspathResource("db.config");
        try {
            properties.load(stream);
        } catch (IOException e) {
            throw new RuntimeException("property file load error.", e);
        } finally {
            FileUtil.closeQuietly(stream);
        }
        return properties.getProperty("db.url");
    }

    private TargetDb findTargetDbAnnotation() {
        for (Annotation annotation : getTestClass().getAnnotations()) {
            if (annotation instanceof TargetDb) {
                return (TargetDb) annotation;
            }
        }
        return null;
    }
}

