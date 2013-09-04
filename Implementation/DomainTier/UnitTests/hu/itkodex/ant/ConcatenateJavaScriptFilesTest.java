package hu.itkodex.ant;

import static com.processpuzzle.litest.matchers.file.FileExist.isExistingFile;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import com.processpuzzle.commons.file.FileHelper;

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.selectors.FilenameSelector;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ConcatenateJavaScriptFilesTest {
   private static final String BASE_DIRECTORY_NAME = "hu/itkodex/ant/";
   private static final String EXPECTED_FILE_NAME = "target/ConcatenatedJavaScript.js";
   private static final String NONE_EXISTING_DIRECTORY = "C:/Temp/NoneExistingDirectory/";
   private static final String NONE_EXISTING_FILE = "noneExisting.file";
   private static final String SOURCE_FILE_SELECTOR = "source/JavaScript_*.js";
   private static final String TARGET_FILE_NAME = "target/Destination.js";
   private String baseDirectory;
   private String projectRootDirectory;
   private ConcatenateJavaScriptFiles concatenationTask;
   private String expectedFilePath;
   private FilenameSelector fileNameSelector;
   private FileSet sourceFileSet;
   private String targetFilePath;
   private Project project;

   @Before
   public void beforeEachTests() {
      configurePathVariables();
      configureProject();
      concatenationTask = new ConcatenateJavaScriptFiles();
      concatenationTask.setProject( project );
      configureFileSelector();
      configureFileSet();
   }
   
   @After
   public void afterEachTests() {
      if( FileHelper.checkFileExist( targetFilePath )) FileHelper.deleteFile( targetFilePath );
   }
   
   @Test
   public void execute_WhenWellConfiguredByFullPath_ConcatenatesSourceFileSet() throws IOException {
      //SETUP:
      concatenationTask.setBaseDirectory( baseDirectory );
      concatenationTask.addFileSet( sourceFileSet );
      concatenationTask.setTargetFile( targetFilePath );
      
      //EXCERCISE:
      concatenationTask.execute();
      
      //VERIFY:
      assertThat( targetFilePath, isExistingFile() );
      assertThat( FileHelper.retrieveFileContentAsString( targetFilePath ), equalTo( FileHelper.retrieveFileContentAsString( expectedFilePath )));
      
      //TEAR DOWN:
      FileHelper.deleteFile( targetFilePath );
   }

   @Test
   public void execute_WhenRelativeTargetFileNameIsGiven_ThenAppendedToBaseDir() throws IOException {
      //SETUP:
      concatenationTask.setBaseDirectory( baseDirectory );
      concatenationTask.addFileSet( sourceFileSet );
      concatenationTask.setTargetFile( TARGET_FILE_NAME );
      
      //EXCERCISE:
      concatenationTask.execute();
      
      //VERIFY:
      assertThat( targetFilePath, isExistingFile() );
      assertThat( FileHelper.retrieveFileContentAsString( targetFilePath ), equalTo( FileHelper.retrieveFileContentAsString( expectedFilePath )));
      
      //TEAR DOWN:
      FileHelper.deleteFile( targetFilePath );
   }

   @Test
   public void execute_WhenBaseDirectoryIsNotGiven_ProjectBaseDirIsUsed() {
      //SETUP
      concatenationTask.addFileSet( sourceFileSet );
      concatenationTask.setTargetFile( targetFilePath );
      when( project.getBaseDir() ).thenReturn( new File( baseDirectory ));
      
      //EXCERCISE:
      concatenationTask.execute();
      
      //VERIFY:
      assertThat( targetFilePath, isExistingFile() );
      assertThat( FileHelper.retrieveFileContentAsString( targetFilePath ), equalTo( FileHelper.retrieveFileContentAsString( expectedFilePath )));
      
      //TEAR DOWN:
      FileHelper.deleteFile( targetFilePath );
   }
   
   @Test
   public void execute_WhenRelativeBaseDirectoryIsGiven_ThenAppendedToProjectBaseDir() {
      //SETUP:
      concatenationTask.setBaseDirectory( BASE_DIRECTORY_NAME );
      concatenationTask.addFileSet( sourceFileSet );
      concatenationTask.setTargetFile( targetFilePath );
      when( project.getBaseDir() ).thenReturn( new File( projectRootDirectory ));
      
      //EXCERCISE:
      concatenationTask.execute();
      
      //VERIFY:
      assertThat( targetFilePath, isExistingFile() );
      assertThat( FileHelper.retrieveFileContentAsString( targetFilePath ), equalTo( FileHelper.retrieveFileContentAsString( expectedFilePath )));
      
      //TEAR DOWN:
      FileHelper.deleteFile( targetFilePath );
   }
   
   @Test( expected = BuildException.class )
   public void execute_WhenSourceFileSetIsMissing_ThrowsExeption() {
      //SETUP:
      concatenationTask.setBaseDirectory( baseDirectory );
      concatenationTask.setTargetFile( targetFilePath );
      
      //EXCERCISE:
      concatenationTask.execute();
   }
   
   @Test( expected = BuildException.class )
   public void execute_WhenSourceFileSetIsInvalid_ThrowsExeption() {
      //SETUP:
      FilenameSelector invalidFileName = new FilenameSelector();
      invalidFileName.setName( NONE_EXISTING_FILE );
      FileSet invalidFileSet = new FileSet();
      invalidFileSet.add( invalidFileName );
      invalidFileSet.setProject( project );
      
      concatenationTask.setBaseDirectory( baseDirectory );
      concatenationTask.addFileSet( invalidFileSet );
      concatenationTask.setTargetFile( targetFilePath );
      
      //EXCERCISE:
      concatenationTask.execute();
   }

   @Test( expected = BuildException.class )
   public void execute_WhenBaseDirNotGivenAndProjectBaseDirIsInvalid_ThrowsExeption() {
      //SETUP:
      concatenationTask.addFileSet( sourceFileSet );
      concatenationTask.setTargetFile( TARGET_FILE_NAME );
      when( project.getBaseDir() ).thenReturn( new File( NONE_EXISTING_DIRECTORY ));
      
      //EXCERCISE:
      concatenationTask.execute();
   }
   
   private void configureFileSelector() {
      fileNameSelector = new FilenameSelector();
      fileNameSelector.setName( SOURCE_FILE_SELECTOR );
   }

   private void configureFileSet() {
      sourceFileSet = new FileSet();
      sourceFileSet.setProject( project );
      sourceFileSet.addFilename( fileNameSelector );
   }

   private void configureProject() {
      project = mock( Project.class );
   }

   private void configurePathVariables() {
      baseDirectory = FileHelper.folderOf( FileHelper.educeRealPathFromClassPath( BASE_DIRECTORY_NAME ));
      projectRootDirectory = StringUtils.substringBefore( baseDirectory, "hu/itkodex/ant" );
      targetFilePath = baseDirectory + "/" + TARGET_FILE_NAME;
      expectedFilePath = baseDirectory + "/" + EXPECTED_FILE_NAME;
   }
}