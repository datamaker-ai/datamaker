/*
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program. If not, see <https://www.gnu.org/licenses/>.
 */

package ai.datamaker.tomcat;

import lombok.NoArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.DefaultResourceLoader;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Objects;

@SpringBootApplication(scanBasePackages = "ca.breakpoints.datamaker")
public class DataMakerApplicationServlet extends SpringBootServletInitializer {

    @NoArgsConstructor
    public static class RunListener implements SpringApplicationRunListener {

        public RunListener(SpringApplication application, String[] args) {
        }

        @Override
        public void contextPrepared(ConfigurableApplicationContext context) {
            // read jars folder path from environment
            String path = context.getEnvironment().getProperty("application.config.path");

            // enumerate jars in the folder
            File[] files = new File(path + "/jar").listFiles((dir, name) -> name.endsWith(".jar"));

            URL[] urls = (URL[]) Arrays.stream(files).map(f -> {
                try {
                    return f.toURI().toURL();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                return null;
            }).filter(Objects::nonNull).toArray(URL[]::new);

            // create a new classloader which contains the jars...
            ClassLoader extendedClassloader = new URLClassLoader(urls, context.getClassLoader());

            // and replace the context's classloader
            ((DefaultResourceLoader) context).setClassLoader(extendedClassloader);

        }
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(DataMakerApplicationServlet.class);
    }

}
