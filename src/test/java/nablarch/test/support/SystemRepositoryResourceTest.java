package nablarch.test.support;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;

import nablarch.core.repository.SystemRepository;
import nablarch.test.support.SystemRepositoryResource;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * {@link SystemRepositoryResource}のテストクラス。
 */
public class SystemRepositoryResourceTest {

    private final SystemRepositoryResource sut = new SystemRepositoryResource("nablarch/test/support/test.xml");

    @Before
    public void setUp() throws Throwable {
        sut.before();
    }

    @After
    public void tearDown() throws Exception {
        sut.after();
    }

    /**
     * リポジトリにロードした情報が、コンポーネント名で取得できること。
     */
    @Test
    public void getComponentByName() throws Throwable {
        assertThat("コンポーネント名でコンポーネントが取得できること", sut.getComponent("arrayList"), is(instanceOf(ArrayList.class)));
    }

    /**
     * タイプ指定でコンポーネントが取得できること
     * @throws Exception
     */
    @Test
    public void getComponentByType() throws Exception {
        assertThat(sut.getComponentByType(ArrayList.class), is(instanceOf(ArrayList.class)));
        assertThat(sut.getComponentByType(StringBuilder.class), is(instanceOf(StringBuilder.class)));
    }

    /**
     * 後処理でリポジトリがクリアされること。
     * @throws Exception
     */
    @Test
    public void clearSystemRepository() throws Exception {
        assertThat("事前チェック：リポジトリにオブジェクトが存在している", sut.getComponent("arrayList"), is(notNullValue()));
        sut.after();
        assertThat("リポジトリ上からオブジェクトがクリアされていること", sut.getComponent("arrayList"), is(nullValue()));
    }
    
    /**
     * コンポーネントが追加されていること
     * @throws Exception
     */
    @Test
    public void addComponent() throws Exception {
    	assertThat("コンポーネントが存在しないこと", SystemRepository.get("testComponent"), is(nullValue()));
    	sut.addComponent("testComponent", new ArrayList<String>());
    	assertThat("コンポーネントが追加されていること", SystemRepository.get("testComponent"), is(instanceOf(ArrayList.class)));
    }
}
