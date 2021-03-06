/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.trans.steps.csvinput;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.step.RowAdapter;
import org.pentaho.di.trans.steps.mock.StepMockHelper;
import org.pentaho.di.trans.steps.textfileinput.TextFileInputField;

/**
 * Tests for double line endings in CsvInput step
 *
 * @author Pavel Sakun
 * @see CsvInput
 */
public class CsvInputDoubleLineEndTest {
  private static final String ASCII = "windows-1252";
  private static final String UTF8 = "UTF-8";
  private static final String UTF16LE = "UTF-16LE";
  private static final String UTF16BE = "UTF-16LE";
  private static final String TEST_DATA = "Header1\tHeader2\r\nValue\tValue\r\nValue\tValue\r\n";

  private static StepMockHelper<?, ?> stepMockHelper;

  @BeforeClass
  public static void setUp() throws KettleException {
    KettleEnvironment.init();
    stepMockHelper =
      new StepMockHelper<CsvInputMeta, CsvInputData>( "CsvInputTest", CsvInputMeta.class, CsvInputData.class );
    when( stepMockHelper.logChannelInterfaceFactory.create( any(), any( LoggingObjectInterface.class ) ) )
      .thenReturn( stepMockHelper.logChannelInterface );
    when( stepMockHelper.trans.isRunning() ).thenReturn( true );
  }

  @Test
  public void testASCII() throws Exception {
    doTest( ASCII, ASCII, TEST_DATA );
  }

  @Test
  public void testUTF16LE() throws Exception {
    doTest( UTF16LE, UTF16LE, TEST_DATA );
  }

  @Test
  public void testUTF16BE() throws Exception {
    doTest( UTF16BE, UTF16BE, TEST_DATA );
  }

  @Test
  public void testUTF8() throws Exception {
    doTest( UTF8, UTF8, TEST_DATA );
  }

  private void doTest( final String fileEncoding, final String stepEncoding, final String testData ) throws Exception {
    String testFilePath = createTestFile( fileEncoding, testData );

    CsvInputMeta meta = createStepMeta( testFilePath, stepEncoding );
    CsvInputData data = new CsvInputData();

    CsvInput csvInput =
      new CsvInput(
        stepMockHelper.stepMeta, stepMockHelper.stepDataInterface, 0, stepMockHelper.transMeta,
        stepMockHelper.trans );

    csvInput.init( meta, data );
    csvInput.addRowListener( new RowAdapter() {
      @Override
      public void rowWrittenEvent( RowMetaInterface rowMeta, Object[] row ) throws KettleStepException {
        for ( int i = 0; i < rowMeta.size(); i++ ) {
          assertEquals( "Value", row[i] );
        }
      }
    } );

    boolean haveRowsToRead;
    do {
      haveRowsToRead = !csvInput.processRow( meta, data );
    } while ( !haveRowsToRead );

    csvInput.dispose( meta, data );
    assertEquals( 2, csvInput.getLinesWritten() );
  }

  private CsvInputMeta createStepMeta( final String testFilePath, final String encoding ) {
    final CsvInputMeta meta = new CsvInputMeta();
    meta.setFilename( testFilePath );
    meta.setDelimiter( "\t" );
    meta.setEncoding( encoding );
    meta.setEnclosure( "\"" );
    meta.setBufferSize( "50000" );
    meta.setInputFields( getInputFileFields() );
    meta.setHeaderPresent( true );
    return meta;
  }

  private String createTestFile( final String encoding, final String content ) throws IOException {
    File tempFile = File.createTempFile( "PDI_tmp", ".tmp" );
    tempFile.deleteOnExit();

    Writer osw = new PrintWriter( tempFile, encoding );
    osw.write( content );
    osw.close();

    return tempFile.getAbsolutePath();
  }

  private TextFileInputField[] getInputFileFields() {
    TextFileInputField field1 = new TextFileInputField();
    field1.setName( "Field1" );
    field1.setType( ValueMetaInterface.TYPE_STRING );

    TextFileInputField field2 = new TextFileInputField();
    field2.setName( "Field2" );
    field2.setType( ValueMetaInterface.TYPE_STRING );

    return new TextFileInputField[] { field1, field2 };
  }
}
