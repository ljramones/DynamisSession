module org.dynamisengine.session.api {
    requires transitive org.dynamisengine.core;
    requires org.dynamisengine.ecs.api;

    exports org.dynamisengine.session.api;
    exports org.dynamisengine.session.api.codec;
    exports org.dynamisengine.session.api.model;
}
