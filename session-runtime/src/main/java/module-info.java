module org.dynamisengine.session.runtime {
    requires transitive org.dynamisengine.session.api;
    requires org.dynamisengine.session.core;
    requires org.dynamisengine.core;
    requires org.dynamisengine.ecs.api;
    requires org.dynamisengine.ecs.core;

    exports org.dynamisengine.session.runtime;
}
