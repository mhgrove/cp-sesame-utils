package com.clarkparsia.sesame;


import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.openrdf.sesame.Sesame;
import org.openrdf.sesame.admin.AdminListener;
import org.openrdf.sesame.admin.DummyAdminListener;
import org.openrdf.sesame.admin.StdOutAdminListener;
import org.openrdf.sesame.config.RepositoryInfo;
import org.openrdf.sesame.constants.QueryLanguage;
import org.openrdf.sesame.constants.RDFFormat;
import org.openrdf.sesame.repository.RepositoryList;
import org.openrdf.sesame.repository.SesameRepository;
import org.openrdf.sesame.repository.SesameService;
import org.openrdf.util.io.IOUtil;
import org.openrdf.model.Graph;
import org.xml.sax.ContentHandler;
import org.xml.sax.helpers.DefaultHandler;

import com.clarkparsia.sesame.utils.QueryResultCounter;
import com.clarkparsia.sesame.utils.SesameUtils;

import com.clarkparsia.utils.BasicUtils;
import com.clarkparsia.utils.HTTPS;
import com.clarkparsia.utils.TableFormatter;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: Clark & Parsia, LLC. <http://www.clarkparsia.com></p>
 *
 * @author Evren sirin
 */
public class SesameClient extends DefaultHandler implements ContentHandler {
    private static final String FORMAT_RDFXML = "rdf/xml";
    private static final String FORMAT_NTRIPLES = "ntriples";
    private static final String FORMAT_N3 = "n3";

    String[] cmd;
    List history;
    BufferedReader in;
    
    int argIndex; 
    
    SesameService service;
    SesameRepository repo;
    
    Map stateInfo;
    
    boolean verbose;
    AdminListener adminListener;
    
    SesameClient() {    
    	history = new ArrayList();
    	in = new BufferedReader( new InputStreamReader( System.in ) );
    	stateInfo = new LinkedHashMap();
    	
    	setVerbose( false );
    }
    
    public void run() {
    	while( true ) {
	    	try {    		
	        	printPrompt();
	        	
	        	readCommand();
	        	
	        	execCommand();
			} catch (EOFException e) {
				System.out.println( "EOF encountered." );
				quit();
			} catch (Exception e) {
				System.out.println( "ERROR: " +  e );
                e.printStackTrace();
            }
    	}
    }
    
    void printPrompt() {
//    	System.out.println();
    	System.out.print( "> " );
    }
    
    void readCommand() throws Exception {
    	String line = in.readLine();
//    	System.out.println();
    	
    	if( line == null )
    		throw new EOFException();
    	
    	cmd = line.split(" ");
    	
    	history.add( cmd );
    }
    
    void execCommand() throws Exception {
    	String cmd = getCommand();
    	
		if( cmd.equals("connect") ) {
			connect();
		}
		else if( cmd.equals("count") ) {
			count();
		}
		else if( cmd.equals("login") ) {
			login();
		}
		else if( cmd.equals("logout") ) {
			logout();
		}
		else if( cmd.equals("list") ) {
			list();
		}
		else if( cmd.equals("help") ) {
			help();
		}
		else if( cmd.equals("select") ) {
			select();
		}
		else if( cmd.equals("status") ) {
			status();
		}
		else if( cmd.equals("clear") ) {
			clear();
		}
		else if( cmd.equals("upload") ) {
			upload();
		}		
		else if( cmd.equals("extract") ) {
			extract();
		}		
		else if( cmd.equals("quit") ) {
			quit();
		}
		else if( cmd.equals("verbose") ) {
			verbose();
		}
        else if (cmd.equals("remove")) {
            remove();
        }
        else
			unknownCommand();
    }
    
    public void connect() throws Exception {
    	String server = getRequiredArg();
            
        service = Sesame.getService( new URL( server ) );
        
        stateInfo.clear();
        stateInfo.put( "Server", server );
        stateInfo.put( "Username", "Not logged in!" );
        stateInfo.put( "Repository", "Not selected!" );        
    }
    
    public void login() throws Exception {
    	checkConnected();
    	
    	final String user = getRequiredArg();
    	final String pass = getRequiredArg();

        java.net.Authenticator.setDefault( new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication( user, pass.toCharArray() );
            }
        } );
    	
    	service.login( user, pass );
    	
    	stateInfo.put( "Username", user );
    }
    
    public void logout() throws Exception {
    	checkConnected();
    	
    	noArgs();
    	
    	if( service == null )
    		throw new IllegalStateException( "Not connected to a sesame api. First use connect!" );
  
    	service.logout();
    	
    	stateInfo.put( "Username", "Not logged in!" );
    }
    
    public void list() throws Exception {
    	checkConnected();
    	
    	noArgs();
    	
    	TableFormatter table = new TableFormatter( new String[] { "Repository", "Permission (rw)" } );
    	
    	RepositoryList list = service.getRepositoryList();
    	for (Iterator i = list.getRepositories().iterator(); i.hasNext();) {
    		RepositoryInfo info = (RepositoryInfo) i.next();
    		
			String permission = "";
			if( info.isReadable() )
				permission += "r";
			
			if( info.isWriteable() )
				permission += "w";
			
			if( permission.length() == 0 )
				permission += "-";
    		
    		List data = new ArrayList();
			data.add( info.getRepositoryId() );
			data.add( permission );	
			
			table.add( data );
		}
    	
    	table.print( System.out );
    }
    
    public void select() throws Exception {
    	checkConnected();
    	
    	String repoName = getRequiredArg();
            
        repo = service.getRepository( repoName );
        
        stateInfo.put( "Repository", repoName );
    }
    
    public void status() {
    	noArgs();
    	
    	if( stateInfo.isEmpty() ) {
    		System.out.println( "Not connected!" );
    		return;
    	}
    	
    	for (Iterator i = stateInfo.keySet().iterator(); i.hasNext();) {
			String prop = (String) i.next();
			String value = (String) stateInfo.get( prop );
		
			System.out.println( prop + ": " + value );
    	}
    }
    
    public void clear() throws Exception {
    	checkSelected();
    	
    	noArgs();
    	
    	repo.clear( adminListener );
    }

    public void remove() throws Exception {
        checkSelected();

        String inFile = getRequiredArg();

        String baseURI = "";
        String aFormat = FORMAT_RDFXML;

        if (hasMoreArgs())
            aFormat = getOptionalArg();

        if( hasMoreArgs() )
        	baseURI = getOptionalArg();

        if( inFile.startsWith( "http://" ) ) {
            Graph aGraph = null;

            if (aFormat.equals(FORMAT_RDFXML))
                aGraph = SesameUtils.rdfToGraph(new URL( inFile ).openStream(), baseURI);
            else if (aFormat.equals(FORMAT_N3))
                aGraph = SesameUtils.turtleToGraph(new URL( inFile ).openStream(), baseURI);
            else if (aFormat.equals(FORMAT_NTRIPLES))
                aGraph = SesameUtils.ntriplesToGraph(new URL( inFile ).openStream(), baseURI);

            if (aGraph != null)
                repo.removeGraph(aGraph);
            else throw new IOException("Invalid format specified for file");
        }
        else {

        	File file = new File( inFile ).getAbsoluteFile();
        	File parent = file.getParentFile();

            if (parent == null || !parent.exists())
				throw new FileNotFoundException( inFile );
			else {

	        	String name = file.getName();

	        	// turn standard wild cards into Java regexp
				name = BasicUtils.replace( name, ".", "\\." );
				name = BasicUtils.replace( name, "?", "." );
	        	name = BasicUtils.replace( name, "*", ".*" );

		        final String filter = name;
				File[] files = parent.listFiles(new FilenameFilter() {
					public boolean accept(File dir, String name) {
						return dir != null && name.matches(filter);
					}
				});

				if( files == null || files.length == 0 )
					throw new FileNotFoundException( inFile );

				for (int i = 0; i < files.length; i++) {
					System.out.println( "Removing " + files[i] + " ("+aFormat+")");

                    Graph aGraph = null;

                    if (aFormat.equals(FORMAT_RDFXML))
                        aGraph = SesameUtils.rdfToGraph(new FileInputStream( files[i] ), baseURI);
                    else if (aFormat.equals(FORMAT_N3))
                        aGraph = SesameUtils.turtleToGraph(new FileInputStream( files[i] ), baseURI);
                    else if (aFormat.equals(FORMAT_NTRIPLES))
                        aGraph = SesameUtils.ntriplesToGraph(new FileInputStream( files[i] ), baseURI);

                    if (aGraph != null)
                        repo.removeGraph(aGraph);
                    else throw new IOException("Invalid format specified for file");
				}
			}
        }
    }

    public void upload() throws Exception {
        checkSelected();
        
        boolean verifyData = true;

        String inFile = getRequiredArg();
        String baseURI = "";
        
        if( hasMoreArgs() )
        	baseURI = getOptionalArg();
        
        if( inFile.startsWith( "http://" ) )
        	repo.addData( new URL( inFile ), baseURI, RDFFormat.RDFXML, verifyData, adminListener );
        else {        	

        	File file = new File( inFile ).getAbsoluteFile();
        	File parent = file.getParentFile();
        	
        	if (parent == null || !parent.exists())
				throw new FileNotFoundException( inFile );
			else {

	        	String name = file.getName();
	        	
	        	// turn standard wild cards into Java regexp
				name = BasicUtils.replace( name, ".", "\\." );
				name = BasicUtils.replace( name, "?", "." );
	        	name = BasicUtils.replace( name, "*", ".*" );

		        final String filter = name;
				File[] files = parent.listFiles(new FilenameFilter() {
					public boolean accept(File dir, String name) {
						return dir != null && name.matches(filter);
					}
				});
				
				if( files == null || files.length == 0 )
					throw new FileNotFoundException( inFile );
				
				for (int i = 0; i < files.length; i++) {
					System.out.println( "Uploading " + files[i] );
					repo.addData( files[i], baseURI, RDFFormat.RDFXML, verifyData, adminListener );
				}
			}
        }
    }
    
    public void extract() throws Exception {
        checkSelected();

        String outFile = getRequiredArg();
        
        InputStream in = repo.extractRDF( RDFFormat.RDFXML, true, true, true, false );
        OutputStream out = new FileOutputStream( outFile );
        
        IOUtil.transfer( in, out );
        
        in.close();
        out.close();
    }
    
	public void count() throws Exception {
    	checkSelected();
    	
    	noArgs();
    	
    	QueryResultCounter counter = new QueryResultCounter();
    	repo.performTableQuery(QueryLanguage.SERQL, "SELECT s FROM {s} p {o}", counter);
    	int count = counter.getCount();
    	
    	System.out.println( "Number of triples: " + count );
    }

    public void verbose() {
    	String onOrOff = getOptionalArg();
    	
    	if( onOrOff == null )
    		setVerbose( !verbose );
    	else if( "on".equals( onOrOff ) )
    		setVerbose( true );
    	else if( "off".equals( onOrOff ) )
    		setVerbose( false );
    	else
    		throw new IllegalArgumentException( "verbose command requires option 'on' or 'off'" );
    }
    
    private void setVerbose( boolean verbose ) {
    	this.verbose = verbose;
    	
    	if( verbose ) {
    		adminListener = new StdOutAdminListener();
    		System.out.println( "Verbose mode turned on" );
    	}
    	else
    		adminListener = new DummyAdminListener();
    }
    
    public void quit() {
    	System.out.println( "Bye." );
    	System.exit( 0 );
    }
    
    public void help() {
    	System.out.println( "Available commands: " );
    	System.out.println( "  connect <serverURL>          - Connect to a Sesame api");
    	System.out.println( "  count                        - Count the no of triples in the repo");
    	System.out.println( "  extract <fileName>           - Extract the contents of the selected");
    	System.out.println( "                                 repository to the given file");
    	System.out.println( "  help                         - Print this screen");
    	System.out.println( "  list                         - List the repositories on the api");
    	System.out.println( "  login <uname> <pwd>          - Login to the api");
    	System.out.println( "  logout                       - Logout the api");
    	System.out.println( "  select <repoID>              - Select a repository");
    	System.out.println( "  status                       - Display state info");
    	System.out.println( "  upload [url|file]            - Upload a file to the selected repo");
    	System.out.println( "                                 (standard Unix wildcards allowed)");
        System.out.println( "  remove [url|file] [format]   - Remove a file from the selected repo");
        System.out.println( "                                 (standard Unix wildcards allowed)");
        System.out.println( "  verbose [on|off]             - Turn verbose mode on/off");
    	System.out.println( "  quit                         - Quit the program");
    }
    
    public void unknownCommand() {
    	System.out.println( "ERROR: Unknown command: " + cmd[0] );
    }
    
    public String getCommand() {
    	if( cmd.length == 0 )
    		throw new IllegalArgumentException( "No command given!" );
    	
    	argIndex = 1;
    	
    	return cmd[0];
    }
    
    public void noArgs() {
    	if( cmd.length > 1 )
    		throw new IllegalArgumentException( "Command " + cmd[0] + " does not expect any arguments" );
    }
    
    public String getRequiredArg() {
    	return getArg( false );
    }
    
    public String getOptionalArg() {
    	return getArg( true );
    }
    
    public boolean hasMoreArgs() {
    	return argIndex < cmd.length;
    }
    
    public String getArg( boolean optional ) {
    	if( cmd.length <= argIndex ) {
    		if( optional )
    			return null;
    		else
    			throw new IllegalArgumentException( "Command " + cmd[0] + " requires at least " + argIndex + " arguments but got " + cmd.length );
    	}
    		
    	return cmd[ argIndex++ ];
    }
    
    public void checkConnected() {
    	if( service == null )
    		throw new IllegalStateException( "Not connected to a sesame api. First use connect!" );
    }
    
    public void checkSelected() {
    	checkConnected();
    	
    	if( repo == null )
    		throw new IllegalStateException( "No repository selected. First use select!" );
    }
    
    public static void usage() {
        System.out.println( "usage: SesameClient" );
        
        System.exit( 0 );
    }
    
    public static void main( String[] args ) throws Exception {
        HTTPS.setup();
    	
        SesameClient client = new SesameClient();
        
        client.run();
    }
}
