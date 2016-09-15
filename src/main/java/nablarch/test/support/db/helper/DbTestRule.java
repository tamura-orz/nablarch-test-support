package nablarch.test.support.db.helper;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import nablarch.core.util.FileUtil;
import nablarch.test.support.db.helper.TargetDb.Db;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class DbTestRule implements TestRule {

    @Override
    public Statement apply(final Statement statement, final Description description) {
        return new Statement() {

            @Override
            public void evaluate() throws Throwable {
            	TargetDb runDb = description.getAnnotation(TargetDb.class);
            	if (runDb == null) {
            		runDb = description.getTestClass().getAnnotation(TargetDb.class);
            	}

                if (runDb == null || canEvaluate(runDb.include(), runDb.exclude())) {
                    statement.evaluate();
                }
            }
        };
    }

    private boolean canEvaluate(Db[] include, Db[] exclude) {
    	
    	String url = getUrl();
    	
    	if (url == null) {
            return false;
        }
    	
    	// includeとexcludeが両方指定された場合はexcludeを優先
    	if (exclude.length != 0) {
    		for (Db db : exclude) {
        		if (url.startsWith(db.prefix)) {
        			return false;
        		}
        	}
    		return true;
    	} else if (include.length != 0) {
    		for (Db db : include) {
    			if (url.startsWith(db.prefix)) {
    				return true;
    			}
    		}
    		return false;
    	} else {
        	// include、excludeのいずれも指定されない場合はテスト実施。
        	return true;
    	}
    	

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
}
