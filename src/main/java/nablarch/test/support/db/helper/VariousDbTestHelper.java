package nablarch.test.support.db.helper;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.EntityManager;
import javax.persistence.FlushModeType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.Transient;
import javax.persistence.spi.PersistenceUnitTransactionType;
import javax.sql.DataSource;

import org.eclipse.persistence.internal.jpa.config.persistenceunit.PersistenceUnitImpl;
import org.eclipse.persistence.internal.jpa.deployment.SEPersistenceUnitInfo;
import org.eclipse.persistence.jpa.JpaHelper;
import org.eclipse.persistence.jpa.config.PersistenceUnit;
import org.eclipse.persistence.jpa.config.RuntimeFactory;
import org.eclipse.persistence.sessions.DatabaseSession;
import org.eclipse.persistence.tools.schemaframework.FieldDefinition;
import org.eclipse.persistence.tools.schemaframework.SchemaManager;
import org.eclipse.persistence.tools.schemaframework.TableDefinition;

import nablarch.core.db.connection.ConnectionFactory;
import nablarch.core.repository.di.DiContainer;
import nablarch.core.util.StringUtil;

public class VariousDbTestHelper {

    /** デフォルトのpersistenceUnit名 */
    private static final String DEFAULT_UNIT_NAME = "defaultPersistenceUnit";

    /** エンティティマネージャ */
    private static EntityManager em;

    private static DataSource dataSource;

    /**
     * このクラスのインスタンスは作成不可。
     */
    private VariousDbTestHelper() {
    }

    public static Connection getNativeConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public static TargetDb.Db getTargetDatabase() throws Exception {
        final Connection connection = dataSource.getConnection();
        final String url;
        try {
            url = connection
                    .getMetaData()
                    .getURL();
        } finally {
            connection.close();
        }
        for (TargetDb.Db db : TargetDb.Db.values()) {
            if (url.startsWith(db.prefix)) {
                return db;
            }
        }
        return null;
    }

    /**
     * 初期化処理を行う。
     *
     * @param container
     */
    public static void initialize(DiContainer container) {

        // 初期化済みであれば終了
        if (em != null) {
            return;
        }

        PersistenceUnit pu = new PersistenceUnitImpl(DEFAULT_UNIT_NAME)
                .setTransactionType(PersistenceUnitTransactionType.RESOURCE_LOCAL)
                .setExcludeUnlistedClasses(false);

        for (String entity : EntityList.get("classpath:entity.list.txt")) {
            pu.setClass(entity.trim());
        }
        dataSource = getDataSource(container);
        ((SEPersistenceUnitInfo) pu.getPersistenceUnitInfo()).setNonJtaDataSource(dataSource);
        pu.addMappings();
        em = RuntimeFactory.getInstance()
                .createEntityManagerFactory(pu)
                .createEntityManager();
        em.setFlushMode(FlushModeType.AUTO);
    }

    /**
     * テーブルを作成する。
     *
     * @param entityClass Entityのクラス
     */
    public static void createTable(Class<?> entityClass) {
        dropTable(entityClass);

        DatabaseSession ds = JpaHelper.getDatabaseSession(em.getEntityManagerFactory());
        SchemaManager sm = new SchemaManager(ds);

        Table table = entityClass.getAnnotation(Table.class);
        String tableName = table.name();
        String schemaName = table.schema();

        TableDefinition td = new TableDefinition();
        td.setName(addSchema(tableName, schemaName));
        td.setCreationSuffix(ds.getPlatform()
                .getTableCreationSuffix());

        for (Field field : entityClass.getFields()) {
            if (field.getAnnotation(Transient.class) != null) {
                continue;
            }

            FieldDefinition fd = new FieldDefinition();

            fd.setIsPrimaryKey(field.getAnnotation(Id.class) != null);
            Column c = field.getAnnotation(Column.class);
            if (c != null) {
                final Temporal temporal = field.getAnnotation(Temporal.class);
                if (temporal != null) {
                    switch (temporal.value()) {
                        case TIMESTAMP:
                            fd.setType(java.sql.Timestamp.class);
                            break;
                        case DATE:
                            fd.setType(java.sql.Date.class);
                            break;
                        case TIME:
                            fd.setType(java.sql.Time.class);
                            break;
                    }
                } else if (isSqlServerBinaryType(field)) {
                    // SqlServerの場合、varbinary(max)になるとInputstreamが返されるので、
                    // 強制的に型を指定
                    if (StringUtil.hasValue(c.columnDefinition())) {
                        fd.setTypeDefinition(c.columnDefinition());
                    } else {
                        fd.setTypeDefinition("varbinary(4000)");
                    }
                } else {
                    fd.setType(field.getType());
                    fd.setTypeDefinition(StringUtil.hasValue(c.columnDefinition()) ? c.columnDefinition() : null);
                }
                fd.setName(c.name());
                fd.setUnique(!fd.isPrimaryKey() && c.unique());
                fd.setShouldAllowNull(fd.isPrimaryKey() ? false : c.nullable());

                if (c.precision() != 0) {
                    fd.setSize(c.precision());
                    fd.setSubSize(c.scale());
                } else {
                    fd.setSize(c.length());
                }
            } else {

                String targetTableName = field.getType()
                        .getAnnotation(Table.class)
                        .name();

                JoinColumn j = field.getAnnotation(JoinColumn.class);
                Field f = getJoinField(field, j.referencedColumnName());
                c = f.getAnnotation(Column.class);

                fd.setType(f.getType());
                fd.setName(j.name());
                fd.setUnique(!fd.isPrimaryKey() && j.unique());
                fd.setShouldAllowNull(j.nullable());
                fd.setTypeDefinition(StringUtil.hasValue(j.columnDefinition()) ? j.columnDefinition() : null);
                if (c.precision() != 0) {
                    fd.setSize(c.precision());
                    fd.setSubSize(c.scale());
                } else {
                    fd.setSize(c.length());
                }
                td.addForeignKeyConstraint(
                        "FK_" + targetTableName + '_' + tableName,
                        j.name(),
                        j.referencedColumnName(),
                        addSchema(targetTableName, schemaName));
            }
            td.addField(fd);
        }
        sm.getSession()
                .beginTransaction();
        try {
            sm.createObject(td);
            sm.createConstraints(td);
            sm.getSession()
                    .commitTransaction();
        } catch (Exception e) {
            sm.getSession()
                    .rollbackTransaction();
            throw new RuntimeException(e);
        }
    }

    private static boolean isSqlServerBinaryType(Field field) {
        try {
            return field.getType().equals(byte[].class) && getTargetDatabase() == TargetDb.Db.SQL_SERVER;
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    /**
     * テーブルを削除する。
     *
     * @param entityClass Entityのクラス
     */
    public static void dropTable(Class<?> entityClass) {
        Table table = entityClass.getAnnotation(Table.class);
        String tableName = table.name();
        String schemaName = table.schema();

        DatabaseSession ds = JpaHelper.getDatabaseSession(em.getEntityManagerFactory());
        SchemaManager sm = new SchemaManager(ds);
        sm.getSession()
                .beginTransaction();
        try {
            sm.dropTable(StringUtil.hasValue(schemaName) ? schemaName + '.' + tableName : tableName);
            sm.getSession()
                    .commitTransaction();
        } catch (Exception e) {
            sm.getSession()
                    .rollbackTransaction();
            // NOP
        }
    }

    /**
     * テーブルを削除し、コミットする。
     *
     * @param entityClass Entityクラス
     */
    public static void delete(Class<?> entityClass) {
        Table table = entityClass.getAnnotation(Table.class);
        String tableName = table.name();
        String schemaName = table.schema();

        em.getTransaction()
                .begin();
        try {
            em.createNativeQuery("DELETE FROM " + addSchema(tableName, schemaName))
                    .executeUpdate();
            em.getTransaction()
                    .commit();
        } catch (Exception e) {
            em.getTransaction()
                    .rollback();
            throw new RuntimeException(e);
        }
    }

    private static String addSchema(String tableName, String schemaName) {
        return StringUtil.hasValue(schemaName) ? schemaName + '.' + tableName : tableName;
    }

    /**
     * テーブルのセットアップを行う。<br />
     * 空の場合はtruncateを使用してください。
     *
     * @param entities 登録するエンティティ
     */
    public static <T> void setUpTable(T... entities) {
        if (entities.length == 0) {
            throw new IllegalArgumentException("please use truncate");
        }
        delete(entities[0].getClass());
        insert(entities);
    }

    /**
     * レコードを登録する。
     *
     * @param array Entityオブジェクトの配列
     */
    public static void insert(Object... array) {
        em.getTransaction()
                .begin();
        try {
            for (Object entity : array) {
                em.persist(entity);
            }
            em.getTransaction()
                    .commit();
        } catch (Exception e) {
            em.getTransaction()
                    .rollback();
            throw new RuntimeException(e);
        }
    }

    /**
     * レコードを更新する。
     *
     * @param array Entityオブジェクトの配列
     */
    public static void update(Object... array) {
        em.getTransaction()
                .begin();
        try {
            for (Object entity : array) {
                em.merge(entity);
            }
            em.getTransaction()
                    .commit();
        } catch (Exception e) {
            em.getTransaction()
                    .rollback();
            throw new RuntimeException(e);
        }
    }

    /**
     * レコードを主キー検索する。
     *
     * @param entityClass Entityクラス
     */
    public static <T> T findById(Class<T> entityClass, Object... ids) {
        return em.find(entityClass, Arrays.asList(ids), new HashMap<String, Object>() {{
            put("javax.persistence.cache.storeMode", "REFRESH");
        }});
    }

    /**
     * レコードを全件検索する。
     *
     * @param entityClass Entityクラス
     * @param orderBy ソート対象のカラム名
     */
    public static <T> List<T> findAll(Class<T> entityClass, String... orderBy) {

        Table table = entityClass.getAnnotation(Table.class);
        String tableName = table.name();
        String schemaName = table.schema();
        String selectQuery = "SELECT * FROM " + (addSchema(tableName, schemaName));

        String orderByQuery = "";
        if (orderBy != null && orderBy.length > 0) {
            StringBuilder sb = new StringBuilder(" ORDER BY ");
            for (String s : orderBy) {
                try {
                    Field f = entityClass.getField(s);
                    Column c = f.getAnnotation(Column.class);
                    if (c == null) {
                        JoinColumn j = f.getAnnotation(JoinColumn.class);
                        sb.append(getJoinField(f, j.referencedColumnName()).getAnnotation(Column.class)
                                .name())
                                .append(", ");
                    } else {
                        sb.append(c.name())
                                .append(", ");
                    }
                } catch (Exception e) {
                    // NOP
                }
            }
            orderByQuery = sb.substring(0, sb.length() - 2);
        }

        return em.createNativeQuery(selectQuery + orderByQuery, entityClass)
                .setHint("javax.persistence.cache.storeMode", "REFRESH")
                .getResultList();
    }

    /**
     * レコードを全件検索する。
     *
     * @param entityClass Entityクラス
     */
    public static <T> List<T> findAll(Class<T> entityClass) {
        return findAll(entityClass, (String[]) null);

    }

    /**
     * DIコンテナからDataSourceオブジェクトを取得する。
     *
     * @param container
     * @return DataSourceオブジェクト
     */
    private static DataSource getDataSource(DiContainer container) {
        ConnectionFactory cf = container.getComponentByName("connectionFactory");

        try {
            Field f = cf.getClass()
                    .getDeclaredField("dataSource");
            f.setAccessible(true);
            return (DataSource) f.get(cf);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Field getJoinField(Field field, String referencedColumnName) {
        for (Field f : field.getType()
                .getFields()) {
            if (referencedColumnName.equals(f.getAnnotation(Column.class)
                    .name())) {
                return f;
            }
        }
        return null;
    }
}
