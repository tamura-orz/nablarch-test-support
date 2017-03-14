package nablarch.test.support.db.datasource;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class DataSourceFactoryTest {

    @Test
    public void createDataSource() throws Exception {
        final DataSourceFactory sut = new DataSourceFactory();
        sut.setUser("sa");
        sut.setPassword("password");
        sut.setUrl("jdbc:h2:mem:test");
        sut.setDriverClassName("org.h2.Driver");

        assertThat("複数回呼び出しても同じインスタンスが戻されること", sut.createObject(), sameInstance(sut.createObject()));
    }
}