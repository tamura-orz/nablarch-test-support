package nablarch.test.support.db.helper;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD,ElementType.TYPE})
public @interface TargetDb {
    
    public enum Db {
        ORACLE("jdbc:oracle"),
        POSTGRE_SQL("jdbc:postgresql"),
        DB2("jdbc:db2"),
        SQL_SERVER("jdbc:sqlserver"),
        MY_SQL("jdbc:mysql"),
        H2("jdbc:h2");

		private Db(String prefix) {
			this.prefix = prefix;
		}
		
        public String prefix;
    }
    
    public Db[] include() default {};
    
    public Db[] exclude() default {};
}
