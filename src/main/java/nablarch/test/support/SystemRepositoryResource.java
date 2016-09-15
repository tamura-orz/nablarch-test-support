package nablarch.test.support;

import java.util.HashMap;
import java.util.Map;

import nablarch.core.repository.ObjectLoader;
import nablarch.core.repository.SystemRepository;
import nablarch.core.repository.di.DiContainer;
import nablarch.core.repository.di.config.DuplicateDefinitionPolicy;
import nablarch.core.repository.di.config.xml.XmlComponentDefinitionLoader;

import org.junit.rules.ExternalResource;

/**
 * システムリポジトリの初期化及び破棄を行うルール実装。
 * <p/>
 * このクラスは、初期処理でコンストラクタで指定されたXMLファイルを元にSystemRepositoryを構築する。
 * 後処理では、構築したSystemRepositoryの情報を破棄する。
 */
public class SystemRepositoryResource extends ExternalResource {

    /** コンポーネント定義のパス */
    private final String componentDefinitionPath;

    /** コンテナ */
    private DiContainer container;

    public SystemRepositoryResource(String componentDefinitionPath) {
        this.componentDefinitionPath = componentDefinitionPath;
    }

    /**
     * 指定されたパスのリポジトリを構築する。
     *
     * @throws Throwable
     */
    @Override
    protected void before() throws Throwable {
        SystemRepository.clear();
        if (componentDefinitionPath == null) {
            return;
        }
        final XmlComponentDefinitionLoader loader = new XmlComponentDefinitionLoader(componentDefinitionPath,
                DuplicateDefinitionPolicy.OVERRIDE);
        container = new DiContainer(loader);
        SystemRepository.load(container);
    }

    /**
     * リポジトリの情報をクリアする。
     */
    @Override
    protected void after() {
        SystemRepository.clear();
    }

    /**
     * コンテナから指定されたタイプのオブジェクトを取得する。
     *
     * @param type タイプ
     * @param <T> 型
     * @return コンテナから取得したオブジェクト
     */
    public <T> T getComponentByType(Class<T> type) {
        if (container == null) {
            return null;
        }
        return container.getComponentByType(type);
    }

    /**
     * リポジトリから指定された名前のオブジェクトを取得する。
     *
     * @param componentName コンポーネント名
     * @param <T> 型
     * @return オブジェクト
     */
    public <T> T getComponent(String componentName) {
        return SystemRepository.get(componentName);
    }

    /**
     * リポジトリに指定した名前でオブジェクトを設定する。
     * 
     * @param name コンポーネント名
     * @param obj オブジェクト
     */
    public void addComponent(final String name, final Object obj) {
        SystemRepository.load(new ObjectLoader() {
			@Override
			@SuppressWarnings("serial")
			public Map<String, Object> load() {
				return new HashMap<String, Object>() {{
					put(name, obj);
				}};
			}
        	
        });
    }
    
    
}

