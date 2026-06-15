package gg.deepsite.pewpew;

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;

@SuppressWarnings("UnstableApiUsage")
public class PewpewPluginLoader implements PluginLoader {

	private static final String[] LIBRARIES = {
			"org.spongepowered:configurate-yaml:4.1.2",
			"org.spongepowered:configurate-core:4.1.2",
			"commons-io:commons-io:2.15.1",
			"org.reflections:reflections:0.10.2"
	};

	@Override
	public void classloader(PluginClasspathBuilder classpathBuilder) {
		MavenLibraryResolver resolver = new MavenLibraryResolver();
		resolver.addRepository(new RemoteRepository.Builder(
				"central", "default", MavenLibraryResolver.MAVEN_CENTRAL_DEFAULT_MIRROR).build());

		for (String library : LIBRARIES) {
			resolver.addDependency(new Dependency(new DefaultArtifact(library), null));
		}

		classpathBuilder.addLibrary(resolver);
	}
}
