package cn.example.base.demo.config.skills;

import cn.example.base.demo.tools.SqlQueryTools;
import com.alibaba.fastjson.JSONObject;
import io.agentscope.core.skill.AgentSkill;
import io.agentscope.core.skill.SkillBox;
import io.agentscope.core.skill.repository.ClasspathSkillRepository;
import io.agentscope.core.tool.Toolkit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.io.IOException;
import java.util.List;

@Slf4j
@Configuration
public class AgentSkillConfig {

    @Autowired
    private SqlQueryTools sqlQueryTools;

    @Bean
    public ClasspathSkillRepository classpathSkillRepository() throws IOException {
        ClasspathSkillRepository repository = new ClasspathSkillRepository("skills");
        List<AgentSkill> allSkills = repository.getAllSkills();
        allSkills.forEach(e -> log.info("classpathSkillRepository: {}", JSONObject.toJSONString(e)));
        return repository;
    }

    @Bean
    public Toolkit sqlQueryToolkit() {
        Toolkit toolkit = new Toolkit();
        toolkit.registerTool(sqlQueryTools);
        return toolkit;
    }

    @Bean
    public SkillBox sqlQuerySkillBox(Toolkit sqlQueryToolkit, ClasspathSkillRepository classpathSkillRepository) {
        SkillBox skillBox = new SkillBox(sqlQueryToolkit);

        List<AgentSkill> allSkills = classpathSkillRepository.getAllSkills();
        allSkills.forEach(agentSkill -> skillBox.registration().skill(agentSkill).apply());

        log.info("allSkills size: {}", allSkills.size());
        return skillBox;
    }

}
