package org.non.web.app;

import org.apache.log4j.BasicConfigurator;
import org.non.web.app.resources.UIResources;

import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class FabricApplication extends Application<Configuration> {

    public static void main(final String[] args) throws Exception {
    	BasicConfigurator.configure();
        new FabricApplication().run("server");
    }

    @Override
    public String getName() {
        return "fabric";
    }

    @Override
    public void initialize(final Bootstrap<Configuration> bootstrap) {
    	bootstrap.addBundle(new AssetsBundle("/web_assets/", "/static"));
    	
    }

    @Override
    public void run(final Configuration configuration,
                    final Environment environment) {
    	environment.jersey().register(new UIResources());
    }

}
