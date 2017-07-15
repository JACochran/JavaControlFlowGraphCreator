package com.basecoverage;

import java.awt.FileDialog;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;

import com.basecoverage.diagram.ControlFlowDiagramGraphFactory;
import com.basecoverage.diagram.exporter.GraphDotExporter;
import com.basecoverage.diagram.exporter.GraphXmlExporter;
import com.basecoverage.reader.ClassFile;
import com.drgarbage.asm.ClassReader;
import com.drgarbage.asm.render.impl.ClassFileDocument;
import com.drgarbage.asm.render.intf.IClassFileDocument;
import com.drgarbage.asm.visitor.FilteringCodeVisitor;
import com.drgarbage.asm.visitor.MethodFilteringVisitor;
import com.drgarbage.controlflowgraph.*;
import com.drgarbage.controlflowgraph.intf.IDirectedGraphExt;
import com.drgarbage.controlflowgraphfactory.export.ExportException;
import com.drgarbage.graph.DefaultGraphSpecification;
import com.drgarbage.visualgraphic.model.ControlFlowGraphDiagram;
import com.drgarbage.visualgraphic.model.VertexBase;

public class BaseCoverageServlet {
	public BaseCoverageServlet() throws ControlFlowGraphException, IOException,
			ExportException {
		// JFileChooser fileDialog = new JFileChooser();
		// JPanel panel = new JPanel();
		//
		// fileDialog.showOpenDialog(panel);
		// File file = fileDialog.getSelectedFile();
		File file = new File(
				"C:\\Users\\Jenifer\\Downloads\\com.drgarbage.controlflowgraphfactory.plugin_4.4.1.201408050542\\com\\drgarbage\\visualgraphic\\model\\ControlFlowGraphDiagram.class");
//		String methodName = "addChild";
//		String className = "ControlFlowGraphDiagram";
//		String packageName = "com.drgarbage.visualgraphic.model";
//		String methodSig = "(Lcom/drgarbage/visualgraphic/model/VertexBase;)Z";
//		FilteringCodeVisitor codeVisitor = getInstructionList(
//				file.getAbsolutePath(), packageName, className, methodName,
//				methodSig);
//		IDirectedGraphExt graph2 = ControlFlowGraphGenerator
//				.generateControlFlowGraph(codeVisitor.getInstructions(),
//						codeVisitor.getLineNumberTable(), true, true, true,
//						false);
//		ControlFlowGraphDiagram diagram = ControlFlowDiagramGraphFactory
//				.createControlFlowGraphDiagram(graph2);
//		
//
//		// source code diagram
//		ControlFlowGraphDiagram diagram2 = ControlFlowDiagramGraphFactory
//				.createSourceCodeGraphDiagram(graph2);
//		File graphXmlFile2 = new File("C:\\Users\\Jenifer\\Desktop\\Graph2.xml");
//
//		for (VertexBase vertex : diagram2.getChildren()) {
//			System.out.println(vertex.getToolTip());
//		}
//		try (Writer buf2 = new FileWriter(graphXmlFile2)) {
//			xmlExport.write(diagram2, buf2);
//		}
		
		final JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new GridLayout(0, 1));
        frame.add(new JButton(new AbstractAction("Load") 
        {

            /**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
            public void actionPerformed(ActionEvent e) 
            {                
                FileDialog fd = new FileDialog(frame, "Choose a file", FileDialog.LOAD);
        		fd.setDirectory("C:\\");
//        		fd.setFile("*.class");
        		fd.setVisible(true);
        		String filename = fd.getFile();
        		if (filename == null)
        		{
        			System.out.println("You cancelled the choice");
        		}
        		else
        		{
        			System.out.println("You chose " + filename);
          		    try {
						exportJavaFile(Paths.get(fd.getDirectory(), filename).toAbsolutePath());
					} catch (FileNotFoundException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (ControlFlowGraphException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
        		}
        		  
            }
        }));
		
		
		
		frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

		

	}
	
	private static void exportJavaFile(Path fileLocation) throws FileNotFoundException, IOException, ControlFlowGraphException
	{
		File file = fileLocation.toFile();
		
		try (FileInputStream inputStream = new FileInputStream(file)) {
			ClassFile classDoc = ClassFile.readClass(inputStream);
			Class<? extends ClassFile> classdd = classDoc.getClass();
			String classesName = classDoc.getClassName();
			System.out.println(classesName);

			FilteringCodeVisitor visitor2 = getInstructionList(
					file.getAbsolutePath(), classDoc.getClassName(),
					classDoc.getClassSimpleName(), classDoc.getMethodSections()
							.get(1).getName(), classDoc.getMethodSections()
							.get(1).getDescriptor());
			IDirectedGraphExt graphFromFile = ControlFlowGraphGenerator
					.generateControlFlowGraph(visitor2.getInstructions(),
							visitor2.getLineNumberTable(), true, true, true,
							false);

			ControlFlowGraphDiagram diagramFromFile = ControlFlowDiagramGraphFactory.buildBasicblockGraphDiagram(visitor2.getInstructions());//ControlFlowDiagramGraphFactory.createControlFlowGraphDiagram(graphFromFile);
			GraphDotExporter dotExporter = new GraphDotExporter();
			dotExporter.setGraphSpecification(new DefaultGraphSpecification());
			File grapdotFile = new File(
					"C:\\Users\\Jenifer\\Desktop\\Graph4.dot");
			grapdotFile.createNewFile();

			try (Writer buf3 = new FileWriter(grapdotFile)) {
				dotExporter.write(diagramFromFile, buf3);
			}
			
			GraphXmlExporter xmlExport = new GraphXmlExporter();
			xmlExport.setGraphSpecification(new DefaultGraphSpecification());
			File graphXmlFile = new File("C:\\Users\\Jenifer\\Desktop\\Graph.xml");
			graphXmlFile.createNewFile();
			try (Writer buf = new FileWriter(graphXmlFile)) {
				xmlExport.write(diagramFromFile, buf);
			}
			
			
			
		}
	}

	/**
	 * Returns instruction list.
	 * 
	 * @param classPath
	 * @param packageName
	 * @param className
	 * @param methodName
	 * @param methodSig
	 * @return instructions
	 * @throws ControlFlowGraphException
	 * @throws IOException
	 */
	private static FilteringCodeVisitor getInstructionList(String filePath,
			String packageName, String className, String methodName,
			String methodSig) throws ControlFlowGraphException, IOException {
		InputStream in = new FileInputStream(filePath);

		FilteringCodeVisitor codeVisitor = new FilteringCodeVisitor(methodName,
				methodSig);
		MethodFilteringVisitor classVisitor = new MethodFilteringVisitor(
				codeVisitor);
		ClassReader cr = new ClassReader(in, classVisitor);
		cr.accept(classVisitor, 0);
		if (codeVisitor.getInstructions() == null) {
			throw new ControlFlowGraphException(
					"ControlFlowGraphGenerator: can't get method info of the "
							+ methodName + methodSig);

		}

		return codeVisitor;
	}

}
