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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.basecoverage.diagram.ControlFlowDiagramGraphFactory;
import com.basecoverage.diagram.exporter.GraphDotExporter;
import com.basecoverage.diagram.exporter.GraphXmlExporter;
import com.basecoverage.reader.ClassFile;
import com.drgarbage.asm.ClassReader;
import com.drgarbage.asm.visitor.FilteringCodeVisitor;
import com.drgarbage.asm.visitor.MethodFilteringVisitor;
import com.drgarbage.controlflowgraph.*;
import com.drgarbage.controlflowgraphfactory.export.ExportException;
import com.drgarbage.graph.DefaultGraphSpecification;
import com.drgarbage.visualgraphic.model.ControlFlowGraphDiagram;

public class BaseCoverageServlet 
{
	public BaseCoverageServlet() throws ControlFlowGraphException, IOException, ExportException 
	{
		//main window
		final JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new GridLayout(3, 3));
		
	    //Xml location output label
		JLabel xmlOutputLocation = new JLabel("C:\\outputGraph.xml");
		frame.add(xmlOutputLocation);
		
		//xml output window
		JTextArea   xmlOutputPreviewTextArea = new JTextArea();
		JScrollPane scrollText = new JScrollPane (xmlOutputPreviewTextArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

		//Xml export location action
		frame.add(new JButton(new AbstractAction("XML Export Location")
															       {
																	    private static final long serialVersionUID = 1L;
														 
														     			@Override
															    		public void actionPerformed(ActionEvent arg0) 
																    	{
														     				setOutputLocationPath(frame, xmlOutputLocation, "*.xml");																		 
																	    }			
																    }));
		frame.add(scrollText);
		
		
		//.dot export
	    //.dot location output label
		JLabel dotOutputLocation = new JLabel("C:\\outputGraph.dot");
		frame.add(dotOutputLocation);
		
		//dot output window
		JTextArea   dotOutputPreviewTextArea = new JTextArea();
		JScrollPane dotScrollText = new JScrollPane (dotOutputPreviewTextArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

		//dot export location action
		frame.add(new JButton(new AbstractAction("DOT Export Location")
															       {
																	    private static final long serialVersionUID = 1L;
														 
														     			@Override
															    		public void actionPerformed(ActionEvent arg0) 
																    	{
														     				setOutputLocationPath(frame, dotOutputLocation, "*.dot");																		 
																	    }			
																    }));
		frame.add(dotScrollText);
		
		//add the ability to load a java class file
		frame.add(new JButton(new AbstractAction("Convert .class File to CFG") 
														{
															private static final long serialVersionUID = 1L;
												
															@Override
															public void actionPerformed(ActionEvent e)
															{
																FileDialog fd = new FileDialog(frame, "Choose a file", FileDialog.LOAD);
																fd.setDirectory("C:\\");
																fd.setFile("*.class");
																fd.setVisible(true);
																String filename = fd.getFile();
																if (filename == null) 
																{
																	System.out.println("You cancelled the choice");
																}
																else 
																{
																	try 
																	{
																		exportJavaFile(Paths.get(fd.getDirectory(), filename).toAbsolutePath(), xmlOutputLocation.getText(), dotOutputLocation.getText());
																		
																		xmlOutputPreviewTextArea.setText(readFile(xmlOutputLocation.getText()));
																		dotOutputPreviewTextArea.setText(readFile(dotOutputLocation.getText()));
																	} 
																	catch (Exception e1) 
																	{
																		e1.printStackTrace();
																	}
																}												
															}
														}));

		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
	
	protected static void setOutputLocationPath(JFrame frame, JLabel outputLocationLabel, String fileFilter)
	{
		FileDialog fd = new FileDialog(frame, "Choose a file", FileDialog.LOAD);
		fd.setDirectory("C:\\");
		fd.setFile(fileFilter);
		fd.setVisible(true);
		String filename = fd.getFile();
		if (filename == null) 
		{
			System.out.println("You cancelled the choice");
		}
		else
		{
			outputLocationLabel.setText(Paths.get(fd.getDirectory(), filename).toAbsolutePath().toString());
		}
	 
	}

	protected String readFile(String path) throws IOException
	{
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded);
	}

	private static void exportJavaFile(Path   javaFileLocation, 
									   String xmlOutputLocation,
									   String dotOutputLocation) throws FileNotFoundException, IOException, ControlFlowGraphException 
	{
		File file = javaFileLocation.toFile();

		try (FileInputStream inputStream = new FileInputStream(file)) 
		{
			ClassFile classDoc = ClassFile.readClass(inputStream);
			//TODO allow user to select the method

			FilteringCodeVisitor methodInstructions = getInstructionList(file.getAbsolutePath(),
														                 classDoc.getMethodSections().get(1).getName(),
															             classDoc.getMethodSections().get(1).getDescriptor());

			ControlFlowGraphDiagram diagramFromFile = ControlFlowDiagramGraphFactory.buildBasicblockGraphDiagram(methodInstructions.getInstructions());
						
			exportToDotFile(diagramFromFile, dotOutputLocation);
			exportToXml(diagramFromFile, xmlOutputLocation);
		}
	}
	
	public static void exportToDotFile(ControlFlowGraphDiagram diagram, String filePath) throws IOException
	{
		GraphDotExporter dotExporter = new GraphDotExporter();
		dotExporter.setGraphSpecification(new DefaultGraphSpecification());
		File grapdotFile = new File(filePath);
		grapdotFile.createNewFile();
		try (Writer buf3 = new FileWriter(grapdotFile))
		{
			dotExporter.write(diagram, buf3);
		}		
	}
	
	public static void exportToXml(ControlFlowGraphDiagram diagram, String filePath) throws IOException
	{
		GraphXmlExporter xmlExport = new GraphXmlExporter();
		xmlExport.setGraphSpecification(new DefaultGraphSpecification());
		File graphXmlFile = new File(filePath);
		graphXmlFile.createNewFile();
		try (Writer buf = new FileWriter(graphXmlFile)) 
		{
			xmlExport.write(diagram, buf);
		}		
	}

	/***
	 * Returns the instructions for a given method in a java .class file
	 * 
	 * @param filePath  The ".class" absolute path
	 * @param methodName  the name of the method
	 * @param methodSig the method signature (i.e. a method with a signature like "public static float abs(float a)" the signature would be"(F)F")
	 * @return filtering code visitor
	 * @throws ControlFlowGraphException
	 * @throws IOException
	 */
	private static FilteringCodeVisitor getInstructionList(String filePath,
														   String methodName, 
														   String methodSig) throws ControlFlowGraphException, IOException 
	{
		try(InputStream fileInputStream = new FileInputStream(filePath))
		{
			FilteringCodeVisitor   codeVisitor  = new FilteringCodeVisitor(methodName,	methodSig);		
			MethodFilteringVisitor classVisitor = new MethodFilteringVisitor(codeVisitor);
			ClassReader            classReader  = new ClassReader(fileInputStream, classVisitor);
			
			classReader.accept(classVisitor, 0);
			
			if (codeVisitor.getInstructions() == null) 
			{
				throw new ControlFlowGraphException("ControlFlowGraphGenerator: can't get method info of the " + methodName + methodSig);

			}

			return codeVisitor;
		}		
	}

}
