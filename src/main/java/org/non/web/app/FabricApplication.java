package org.non.web.app;

import org.non.web.app.resources.UIResources;

import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class FabricApplication extends Application<FabricConfiguration> {

    public static void main(final String[] args) throws Exception {
        new FabricApplication().run("server");
    }

    @Override
    public String getName() {
        return "fabric";
    }

    @Override
    public void initialize(final Bootstrap<FabricConfiguration> bootstrap) {
        // TODO: application initialization
    	bootstrap.addBundle(new AssetsBundle("/web_assets/", "/static"));
    	
    }

    @Override
    public void run(final FabricConfiguration configuration,
                    final Environment environment) {
        // TODO: implement application
    	environment.jersey().register(new UIResources());
    }

}
