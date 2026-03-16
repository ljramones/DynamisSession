module org.dynamisengine.session.core {
    requires transitive org.dynamisengine.session.api;
    requires org.dynamisengine.core;
    requires org.dynamisengine.ecs.api;

    exports org.dynamisengine.session.core;
    exports org.dynamisengine.session.core.codec;
    exports org.dynamisengine.session.core.io;
}
