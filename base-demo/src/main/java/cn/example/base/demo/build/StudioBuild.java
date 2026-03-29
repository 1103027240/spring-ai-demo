package cn.example.base.demo.build;

import io.agentscope.core.studio.StudioManager;
import org.springframework.stereotype.Component;

@Component
public class StudioBuild {

    public void initStudio(String project) {
        StudioManager.init()
                .studioUrl("http://localhost:3000")
                .project(project)
                .runName("run_" + System.currentTimeMillis())
                .initialize()
                .block();
    }

}
