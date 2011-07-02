package hu.itkodex.ant;

import hu.itkodex.commons.file.FileHelper;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.resources.FileResource;

public class ConcatenateJavaScriptFiles extends Task{
   public static final String LINE_BREAKS = "\r\n\r\n";
   public static final String SEPARATOR = "/*** {0} ***/\r\n";
   private File baseDirectory;
   private String baseDirectoryPath;
   private StringBuilder concatenatedContent = new StringBuilder();
   private List<File> sourceFiles = new ArrayList<File>();
   private Vector<FileSet> sourceFileSets = new Vector<FileSet>();
   private File targetFile;
   private String targetFilePath;
   
   @Override
   public void execute() throws BuildException {
      super.execute();
      log( "Starting to concatenate JavaScript files. Base directory: '" + baseDirectoryPath + "', target file path: '" + targetFilePath + "'.", Project.MSG_VERBOSE );
      determineBaseDirectory();
      determineTargetFile();
      determineSourceFiles();
      concatenateSourceFileContent();
      writeConcatenatedContentToTargetFile();
   }

   //Properties
   public void addFileSet( FileSet sourceFileSet ) { this.sourceFileSets.add( sourceFileSet ); }
   public void setBaseDirectory( String baseDirectory ) { this.baseDirectoryPath = baseDirectory; }
   public void setTargetFile( String targetFilePath ) { this.targetFilePath = targetFilePath; }

   //Protected, private helper methods
   private void concatenateSourceFileContent() {
      log( "Starting to concatenate source files.", Project.MSG_DEBUG );
      
      for( File sourceJavaScriptFile : sourceFiles ){
         try{
            String sourceFileContent = FileUtils.readFileToString( sourceJavaScriptFile );
            String separator = MessageFormat.format( SEPARATOR, sourceJavaScriptFile.getName() );
            concatenatedContent.append( separator );
            concatenatedContent.append( sourceFileContent );
            concatenatedContent.append( LINE_BREAKS );
         }catch( IOException e ){
            throw new BuildException( "Reading content of file: '" + sourceJavaScriptFile.getPath() + "' failed.", e );
         }
      }
   }
   
   private void determineBaseDirectory() {
      log( "Starting to determining base directory where task's basedir='" + baseDirectoryPath + "' and project's basedir='" + getProject().getBaseDir() + "'", Project.MSG_DEBUG );
      
      if( baseDirectoryPath == null ){
         baseDirectory = getProject().getBaseDir();
         if( !baseDirectory.exists() ) throw new BuildException( "BaseDirectory attribute is not given and Project.baseDir not exist." );
         
         try{
            baseDirectoryPath = baseDirectory.getCanonicalPath();
            log( "Project's basedir becomes the task's base directory.", Project.MSG_DEBUG );
         }catch( IOException e ){
            throw new BuildException( "Unexpected error occured when accessing Project.baseDir.", e );
         }
      }else {
         baseDirectory = new File( baseDirectoryPath );

         if( !baseDirectory.exists() ) {
            baseDirectoryPath = FilenameUtils.normalize( getProject().getBaseDir().getAbsolutePath() ) + File.separatorChar + baseDirectoryPath;
            baseDirectory = new File( baseDirectoryPath );
            if( !baseDirectory.exists() )
               throw new BuildException( "The given baseDirectoryPath: '" + baseDirectoryPath + "' dosn't exist." );
            
            log( "Project's basedir plus task's relative path becomes the task's base directory.", Project.MSG_DEBUG );
         }else {
            log( "Task's basedir is absolute path and used without modificaiton.", Project.MSG_DEBUG );
         }
      }
      
      log( "Base directory is:'" + baseDirectory.getAbsolutePath() + "'" );
   }

   @SuppressWarnings("unchecked")
   private void determineSourceFiles() {
      log( "Starting to determining source files.", Project.MSG_DEBUG );
      
      if( sourceFileSets.size() == 0 ) throw new BuildException( "Source FileSet is missing." );
         
      for( FileSet fileSet : sourceFileSets ){
         log( "Processing fileset:'" + fileSet.getDescription() + "'", Project.MSG_DEBUG );

         fileSet.setDir( new File( baseDirectoryPath ));
         Iterator<FileResource> fileSetIterator = fileSet.iterator(); 
         while( fileSetIterator.hasNext() ){
            FileResource fileResource = fileSetIterator.next();
            sourceFiles.add( fileResource.getFile() );
         }
      }
      
      if( sourceFiles.size() == 0 ) throw new BuildException( "No source files are specified or they are invalid" );
      log( "Number of source files is:'" + sourceFiles.size() + "'", Project.MSG_DEBUG );
   }

   private void determineTargetFile() {
      log( "Starting to determining target file.", Project.MSG_DEBUG );
      if( targetFilePath == null ) throw new BuildException( "TargetFile attribute is missing." );
      
      targetFile = FileHelper.openOrCreateNewFile( baseDirectoryPath, targetFilePath );
      log( "Target file is:'" + targetFile.getAbsolutePath() + "'" );
   }

   private void writeConcatenatedContentToTargetFile() {
      log( "Starting to save target file:'" + targetFile.getAbsolutePath() + "'", Project.MSG_DEBUG );
      try{
         FileUtils.writeStringToFile( targetFile, concatenatedContent.toString() );
      }catch( IOException e ){
         throw new BuildException( "Writing concatenated content to file: '" + targetFilePath + "' failed.", e );
      }
      
      log( "Target file:'" + targetFile.getAbsolutePath() + "' saved.", Project.MSG_DEBUG );
   }

}
