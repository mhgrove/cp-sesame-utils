package com.clarkparsia.sesame;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JDialog;
import javax.swing.JTextField;
import javax.swing.JPasswordField;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.WindowConstants;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.io.File;
import java.io.FileInputStream;
import java.net.URL;

import org.openrdf.sesame.repository.SesameRepository;
import org.openrdf.sesame.repository.SesameService;
import org.openrdf.sesame.constants.RDFFormat;
import org.openrdf.sesame.constants.QueryLanguage;
import org.openrdf.sesame.admin.StdOutAdminListener;
import org.openrdf.sesame.Sesame;
import org.openrdf.sesame.query.QueryResultsTable;
import org.openrdf.model.Value;
import org.openrdf.model.URI;
import org.openrdf.model.Graph;
import org.openrdf.model.impl.ValueFactoryImpl;
import com.clarkparsia.sesame.utils.SesameUtils;
import com.clarkparsia.utils.net.VisualAuthenticator;

/**
 * Title: <br>
 * Description: <br>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com> <br>
 * Created: Oct 16, 2006 11:27:35 AM
 *
 * @author Michael Grove <mhgrove@hotmail.com>
 */
public class SesameConsole extends JFrame implements ActionListener {
    private static final String CMD_EXIT = "CMD_EXIT";
    private static final String CMD_CONNECT = "CMD_CONNECT";
    private static final String CMD_UPLOAD = "CMD_UPLOAD";

    private SesameRepository mRepo;
    private JFileChooser mFileChooser;

    public SesameConsole() {
        // TODO: save repositories to prefs
        // TODO: use wait cursor when appropriate

        mFileChooser = new JFileChooser(new File(System.getProperty("user.dir")));

        initMenu();
        initGUI();
    }

    public void actionPerformed(ActionEvent theEvent) {
        String aCommand = theEvent.getActionCommand();

        if (aCommand.equals(CMD_EXIT))
            doExit();
        else if (aCommand.equals(CMD_CONNECT))
            doConnect();
        else if (aCommand.equals(CMD_UPLOAD))
            doUpload();
    }

    private void doExit() {
        System.exit(0);
    }

    private void doConnect() {
        new ConnectDialog().setVisible(true);
    }

    private void doUpload() {
        if (mRepo == null) {
            JOptionPane.showMessageDialog(this, "Cannot upload data, no current repository!", "Upload Data", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int aConfirm = mFileChooser.showOpenDialog(this);
        if (aConfirm == JFileChooser.APPROVE_OPTION) {
            File aLocation = mFileChooser.getSelectedFile();

            aConfirm = JOptionPane.showConfirmDialog(this, "Upload file '" + aLocation.getName() + "' to Current Repository?", "Upload Data", JOptionPane.YES_NO_OPTION);
            if (aConfirm == JOptionPane.YES_OPTION) {
                try {
System.err.println("starting add data");
                    SesameRepository aRepo = Sesame.getService().createRepository("test", false);
System.err.println(aLocation);
                    aRepo.addData(new FileInputStream(aLocation), aLocation.toURI().toString(), RDFFormat.RDFXML, false, new StdOutAdminListener());
System.err.println("done step one");
                    mRepo.addData(aRepo, new StdOutAdminListener());

                    //mRepo.addData(new FileInputStream(aLocation), aLocation.toURI().toString(), RDFFormat.RDFXML, false, new StdOutAdminListener());
System.err.println("ending add data");
                    JOptionPane.showMessageDialog(this, "Upload Complete", "Upload Data", JOptionPane.ERROR_MESSAGE);
                }
                catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "An error occurred while uploading data.  Message was: "+ex.getMessage(), "Upload Data", JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            }
        }
    }

    private void initGUI() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        setSize(320, 240);
    }

    private void initMenu() {
        JMenuBar aMenuBar = new JMenuBar();

        JMenu aFileMenu = new JMenu("File");

        JMenuItem aExitItem = new JMenuItem("Exit");
        aExitItem.addActionListener(this);
        aExitItem.setActionCommand(CMD_EXIT);

        JMenuItem aConnectItem = new JMenuItem("Connect...");
        aConnectItem.addActionListener(this);
        aConnectItem.setActionCommand(CMD_CONNECT);

        JMenuItem aUploadItem = new JMenuItem("Upload Data...");
        aUploadItem.addActionListener(this);
        aUploadItem.setActionCommand(CMD_UPLOAD);


        aFileMenu.add(aConnectItem);
        aFileMenu.add(new JSeparator());
        aFileMenu.add(aUploadItem);
        aFileMenu.add(new JSeparator());
        aFileMenu.add(aExitItem);

        aMenuBar.add(aFileMenu);

        setJMenuBar(aMenuBar);
    }

    private void setRepo(String theName, SesameRepository theRepo) {
        mRepo = theRepo;

        setTitle("Connected to: "+theName);
    }

    private class ConnectDialog extends JDialog {
        private JTextField mName;
        private JTextField mServer;
        private JTextField mTable;
        private JTextField mUser;
        private JTextField mPass;

        public ConnectDialog() {
            super((JFrame)null, "Connect to Repository", true);

            mName = new JTextField();
            mServer = new JTextField();
            mTable = new JTextField();
            mUser = new JTextField();
            mPass = new JPasswordField();

            getContentPane().setLayout(new GridBagLayout());

            getContentPane().add(new JLabel("Name:"), new GridBagConstraints(0, 0, 1, 1, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
            getContentPane().add(mName, new GridBagConstraints(1, 0, 1, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

            getContentPane().add(new JLabel("Server:"), new GridBagConstraints(0, 1, 1, 1, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
            getContentPane().add(mServer, new GridBagConstraints(1, 1, 1, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

            getContentPane().add(new JLabel("Table Name:"), new GridBagConstraints(0, 2, 1, 1, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
            getContentPane().add(mTable, new GridBagConstraints(1, 2, 1, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

            getContentPane().add(new JLabel("User:"), new GridBagConstraints(0, 3, 1, 1, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
            getContentPane().add(mUser, new GridBagConstraints(1, 3, 1, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

            getContentPane().add(new JLabel("Password:"), new GridBagConstraints(0, 4, 1, 1, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
            getContentPane().add(mPass, new GridBagConstraints(1, 4, 1, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

            JButton aOkButton = new JButton("Ok");
            aOkButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent theEvent) {
                    try {
                        SesameService aService = Sesame.getService(new URL(mServer.getText()));

                        aService.login(mUser.getText(), mPass.getText());

                        setRepo(mName.getText(), aService.getRepository(mTable.getText()));

                        dispose();
                    }
                    catch (Exception ex) {
                        JOptionPane.showMessageDialog(null, "An error occurred while connecting to the specified repository", "Connect to Repository", JOptionPane.ERROR_MESSAGE);
                        ex.printStackTrace();
                    }
                }
            });

            getContentPane().add(aOkButton, new GridBagConstraints(1, 5, 1, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

            pack();
        }
    }

    private static String cleanup(String theString) {
        theString = theString.replaceAll(";","_");
        theString = theString.replaceAll("/","_");
        theString = theString.replaceAll("\\\\","_");
        return theString;
    }

    private static Value cleanValue(Value theValue) {
        if (theValue instanceof URI) {
            URI aURI = (URI) theValue;
            
            return new ValueFactoryImpl().createURI(cleanup(aURI.getURI()));
        }
        else return theValue;
    }

    public static void main(String[] theArgs) throws Exception {

        if (true) {

		String s = "select  distinct uri, aLabel\n" +
				   "from\n" +
				   "{goal_base} <http://lurch.hq.nasa.gov/2005/11/21/pops#holdsCompetency> {phantom0},\n" +
				   "{phantom0} <http://lurch.hq.nasa.gov/2005/11/21/pops#usesCompetency> {var2},\n" +
				   "{goal_base} <http://lurch.hq.nasa.gov/2005/11/21/pops#worksAt> {uri},\n" +
				   "{goal_base} <http://lurch.hq.nasa.gov/2005/11/21/pops#worksOnProject> {var1},\n" +
				   "{uri} <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> {<http://lurch.hq.nasa.gov/2005/11/21/pops#Center>},\n" +
				   "{var1} <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> {<http://lurch.hq.nasa.gov/2005/11/21/pops/project>},\n" +
				   "{var2} <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> {<http://lurch.hq.nasa.gov/2005/11/21/pops#Competency>},\n" +
				   "{goal_base} <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> {<http://xmlns.com/foaf/0.1/Person>},\n" +
				   "[{uri} <http://www.w3.org/2000/01/rdf-schema#label> {aLabel}]\n" +
				   " limit 10000";

        SesameService aService = Sesame.getService(new URL("https://cyrus.hq.nasa.gov/sesame"));
			aService.login("pops", "nasadev");

        SesameRepository aRepo = aService.getRepository("pops-mem-rdf-db");

//        String aQuery = "select distinct uri, aLabel from {uri} rdf:type {<http://lurch.hq.nasa.gov/2005/11/21/pops/project>}, {uri} rdfs:label {aLabel} where aLabel like \"*new work*\" ignore case";

        QueryResultsTable aResults = aRepo.performTableQuery(QueryLanguage.SERQL, s);

        System.err.println(aResults);

//        String aConstruct = "construct {uri} p {o}, {subj} pred {uri} from {uri} rdf:type {<http://lurch.hq.nasa.gov/2005/11/21/pops/project>}, {uri} rdfs:label {aLabel}, [{uri} p {o}], [{subj} pred {uri}] where aLabel like \"*new work*\" ignore case";
//
//        Graph aGraph = aRepo.performGraphQuery(QueryLanguage.SERQL, aConstruct);
//System.err.println(aGraph.getStatements().hasNext());
//        System.err.println(SesameUtils.graphAsTurtle(aGraph));
        
        System.err.println("done?");
        System.err.println("---------");

//            if (aGraph.getStatements().hasNext()) {
//                System.err.println("trying removal");
//                aRepo.removeGraph(aGraph);
//System.err.println("done remove");
//                System.err.println(aRepo.performTableQuery(QueryLanguage.SERQL, aQuery));
//
//            }
//            System.err.println("---------");

//            System.err.println(aRepo.performTableQuery(QueryLanguage.SERQL, "select uri, aLabel, employs from {uri} rdf:type {<http://lurch.hq.nasa.gov/2005/11/21/pops/project>}, {uri} <http://lurch.hq.nasa.gov/2005/11/21/pops#projectEmploys> {employs}, {uri} rdfs:label {aLabel} limit 50"));

//            System.err.println(aRepo.performTableQuery(QueryLanguage.SERQL, "select distinct uri, aLabel, employs from {uri} rdf:type {<http://lurch.hq.nasa.gov/2005/11/21/pops/project>}, {employs} <http://lurch.hq.nasa.gov/2005/11/21/pops#worksOnProject> {uri}, {uri} rdfs:label {aLabel} limit 50"));

            return;
        }

//        if (true) {
//            RdfRepository aSource = new RdfRepository();
//            aSource.startTransaction();
//
//            aSource.addStatement(aSource.createURI("http://example.org/foo"),
//                                 aSource.createURI("http://example.org/bar"),
//                                 aSource.createLiteral("36.0", aSource.getValueFactory().createURI(XmlSchema.DOUBLE)));
//            aSource.addStatement(aSource.createURI("http://example.org/foo1"),
//                                 aSource.createURI("http://example.org/bar"),
//                                 aSource.createLiteral("46.0", aSource.getValueFactory().createURI(XmlSchema.DOUBLE)));
//            aSource.addStatement(aSource.createURI("http://example.org/foo2"),
//                                 aSource.createURI("http://example.org/bar"),
//                                 aSource.createLiteral("26.0", aSource.getValueFactory().createURI(XmlSchema.DOUBLE)));
//
//            aSource.commitTransaction();
//
//
//            Query aQuery = new SerqlParser(new StringReader("select distinct * from \n" +
//                                                            "{a} <http://example.org/bar> {b}\n" +
//                                                            "where\n" +
//                                                            "b = \"36.0\"^^<http://www.w3.org/2001/XMLSchema#double>")).parseTableQuery().getQuery();
//
//// we expected to see "qa: http://example.org/foo, 36.0," printed
//            aQuery.evaluate(aSource, new QueryAnswerListener() {
//                public boolean queryAnswer(QueryAnswer qa) throws IOException {
//                    System.err.print("qa: ");
//                    for (int i = 0; i < qa.getValueCount(); i++)
//                        System.err.print(qa.getValue(i) + ", ");
//                    System.err.println();
//
//                    return true;
//                }
//
//                public void clear() {
//                }
//            });
//
//            Graph aGraph = new GraphImpl();
//            aGraph.add(aGraph.getValueFactory().createURI("http://example.org/foo"),
//                       aGraph.getValueFactory().createURI("http://example.org/bar"),
//                       aGraph.getValueFactory().createLiteral("36.0", aGraph.getValueFactory().createURI(XmlSchema.DOUBLE)));
//            aGraph.add(aGraph.getValueFactory().createURI("http://example.org/foo1"),
//                       aGraph.getValueFactory().createURI("http://example.org/bar"),
//                       aGraph.getValueFactory().createLiteral("46.0", aGraph.getValueFactory().createURI(XmlSchema.DOUBLE)));
//            aGraph.add(aGraph.getValueFactory().createURI("http://example.org/foo2"),
//                       aGraph.getValueFactory().createURI("http://example.org/bar"),
//                       aGraph.getValueFactory().createLiteral("26.0", aGraph.getValueFactory().createURI(XmlSchema.DOUBLE)));
//
//            SesameRepository aRepo = Sesame.getService().createRepository("test", false);
//            aRepo.addGraph(aGraph);
//
//// we expect 1 to be printed
//            System.err.println(aRepo.performTableQuery(QueryLanguage.SERQL, "select distinct * from \n" +
//                                                                            "{a} <http://example.org/bar> {b}\n" +
//                                                                            "where\n" +
//                                                                            "b = \"36.0\"^^<http://www.w3.org/2001/XMLSchema#double>").getRowCount());
//
//            return;
//        }

        java.net.Authenticator.setDefault(new VisualAuthenticator());

//        new SesameConsole().setVisible(true);


    }
}
