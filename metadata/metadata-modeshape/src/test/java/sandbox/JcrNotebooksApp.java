package sandbox;

/*-
 * #%L
 * kylo-metadata-modeshape
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import com.thinkbiganalytics.metadata.api.project.Project;
import com.thinkbiganalytics.metadata.modeshape.project.JcrProject;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;

import org.apache.commons.io.IOUtils;
import org.modeshape.common.collection.Problems;
import org.modeshape.jcr.ModeShapeEngine;
import org.modeshape.jcr.RepositoryConfiguration;
import org.modeshape.jcr.api.JcrTools;
import org.modeshape.jcr.api.Repository;
import org.modeshape.jcr.api.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

public class JcrNotebooksApp {

    public static Logger logger = LoggerFactory.getLogger(JcrNotebooksApp.class);


    public static void main(String... args) throws RepositoryException, IOException {

        ModeShapeEngine engine = new ModeShapeEngine();
        engine.start();

        Repository repository = null;
        String repositoryName = null;

        URL url = JcrNotebooksApp.class.getClassLoader().getResource("sandbox/notebook-test-repository.json");
        RepositoryConfiguration config = RepositoryConfiguration.read(url);

        // Verify the configuration for the repository ...
        Problems problems = config.validate();
        if (problems.hasErrors()) {
            System.err.println("Problems starting the engine.");
            System.err.println(problems);
            System.exit(-1);
        }

        // Deploy the repository ...
        repository = engine.deploy(config);
        repositoryName = config.getName();

        Session session = null;
        JcrTools tools = new JcrTools();
        tools.setDebug(true);

        // get the repository
        repository = engine.getRepository(repositoryName);

        Project p;

        session = repository.login("default");

        // Create the '/files' node that is an 'nt:folder' ...
        Node root = session.getRootNode();

        /*
        Node filesNode = root.addNode("notebooks", "nt:folder");

        InputStream stream =
            new BufferedInputStream(new FileInputStream("/Users/th186036/filesystemconnector/hello.txt"));

        // Create an 'nt:file' node at the supplied path ...
        Node fileNode = filesNode.addNode("hello.txt", "nt:file");

        // Upload the file to that node ...
        Node contentNode = fileNode.addNode("jcr:content", "nt:resource");
        Binary binary = session.getValueFactory().createBinary(stream);
        contentNode.setProperty("jcr:data", binary);

        session.save();
        */


        session = repository.login("default");

        Node project = JcrUtil.getOrCreateNode(root, "Project1", JcrProject.NODE_TYPE);

        List<Node> nodes = JcrUtil.getNodesOfType(root, JcrProject.NODE_TYPE);
        logger.debug("Node list {}", nodes );

        //List<JcrEntity> fileNodes = JcrUtil.getChildrenMatchingNodeType(root, JcrProject.NODE_TYPE, JcrEntity.class, null);
        //logger.debug("Node list {}", nodes );

        logger.debug( "Modeshape subgraph" );
        tools.printSubgraph(root);


        if (session != null) {
            session.logout();
        }
        logger.info("Shutting down engine ...");
        try {
            engine.shutdown().get();
            logger.info("Success!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static String dumpJcrFile(Session session, String path ) throws RepositoryException, IOException {
        // Retrieve FILES!!
        Node doc = session.getNode(path);
        Node imageContent = doc.getNode("jcr:content");
        Binary content = imageContent.getProperty("jcr:data").getBinary();
        InputStream is = content.getStream();

        // NB: does not close inputStream, you can use IOUtils.closeQuietly for that
        String theString = IOUtils.toString(is, "UTF-8");

        // clean up
        IOUtils.closeQuietly(is);

        return theString;
    }
}
