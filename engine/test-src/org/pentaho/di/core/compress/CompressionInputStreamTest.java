package org.pentaho.di.core.compress;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.di.core.plugins.PluginRegistry;

public class CompressionInputStreamTest {

  public static final String PROVIDER_NAME = "None";

  public CompressionProviderFactory factory = null;
  public CompressionInputStream inStream = null;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    PluginRegistry.addPluginType( CompressionPluginType.getInstance() );
    PluginRegistry.init( true );
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
  }

  @Before
  public void setUp() throws Exception {
    factory = CompressionProviderFactory.getInstance();
    CompressionProvider provider = factory.getCompressionProviderByName( PROVIDER_NAME );
    ByteArrayInputStream in = createTestInputStream();
    inStream = new DummyCompressionIS( in, provider );
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testCtor() {
    assertNotNull( inStream );
  }

  @Test
  public void getCompressionProvider() {
    CompressionProvider provider = inStream.getCompressionProvider();
    assertEquals( provider.getName(), PROVIDER_NAME );
  }

  @Test
  public void testNextEntry() throws IOException {
    assertNull( inStream.nextEntry() );
  }

  @Test
  public void testClose() throws IOException {
    CompressionProvider provider = inStream.getCompressionProvider();
    ByteArrayInputStream in = createTestInputStream();
    inStream = new DummyCompressionIS( in, provider );
    inStream.close();
  }

  @Test
  public void testRead() throws IOException {
    CompressionProvider provider = inStream.getCompressionProvider();
    ByteArrayInputStream in = createTestInputStream();
    inStream = new DummyCompressionIS( in, provider );
    assertEquals( inStream.available(), inStream.read( new byte[ 100 ], 0, inStream.available() ) );
  }

  @Test
  public void delegatesReadBuffer() throws Exception {
    ByteArrayInputStream in = createTestInputStream();
    in = spy( in );
    inStream = new DummyCompressionIS( in, inStream.getCompressionProvider() );
    inStream.read( new byte[ 16 ] );
    verify( in ).read( any( byte[].class ) );
  }

  @Test
  public void delegatesReadBufferWithParams() throws Exception {
    ByteArrayInputStream in = createTestInputStream();
    in = spy( in );
    inStream = new DummyCompressionIS( in, inStream.getCompressionProvider() );
    inStream.read( new byte[ 16 ], 0, 16 );
    verify( in ).read( any( byte[].class ), anyInt(), anyInt() );
  }


  private static ByteArrayInputStream createTestInputStream() {
    return new ByteArrayInputStream( "Test".getBytes() );
  }

  private static class DummyCompressionIS extends CompressionInputStream {
    public DummyCompressionIS( InputStream in, CompressionProvider provider ) {
      super( in, provider );
    }
  }
}
