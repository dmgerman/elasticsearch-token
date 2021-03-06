begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.codec
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|codec
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|codecs
operator|.
name|Codec
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|codecs
operator|.
name|lucene50
operator|.
name|Lucene50StoredFieldsFormat
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|codecs
operator|.
name|lucene50
operator|.
name|Lucene50StoredFieldsFormat
operator|.
name|Mode
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|codecs
operator|.
name|lucene62
operator|.
name|Lucene62Codec
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|codecs
operator|.
name|lucene70
operator|.
name|Lucene70Codec
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|document
operator|.
name|Document
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|index
operator|.
name|DirectoryReader
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|index
operator|.
name|IndexWriter
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|index
operator|.
name|IndexWriterConfig
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|index
operator|.
name|SegmentReader
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|store
operator|.
name|Directory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|util
operator|.
name|LuceneTestCase
operator|.
name|SuppressCodecs
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|logging
operator|.
name|ESLoggerFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|settings
operator|.
name|Settings
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|env
operator|.
name|Environment
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|IndexSettings
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|analysis
operator|.
name|IndexAnalyzers
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|mapper
operator|.
name|MapperService
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|similarity
operator|.
name|SimilarityService
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|indices
operator|.
name|mapper
operator|.
name|MapperRegistry
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|ESTestCase
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|IndexSettingsModule
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|instanceOf
import|;
end_import

begin_class
annotation|@
name|SuppressCodecs
argument_list|(
literal|"*"
argument_list|)
comment|// we test against default codec so never get a random one here!
DECL|class|CodecTests
specifier|public
class|class
name|CodecTests
extends|extends
name|ESTestCase
block|{
DECL|method|testResolveDefaultCodecs
specifier|public
name|void
name|testResolveDefaultCodecs
parameter_list|()
throws|throws
name|Exception
block|{
name|CodecService
name|codecService
init|=
name|createCodecService
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|codecService
operator|.
name|codec
argument_list|(
literal|"default"
argument_list|)
argument_list|,
name|instanceOf
argument_list|(
name|PerFieldMappingPostingFormatCodec
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|codecService
operator|.
name|codec
argument_list|(
literal|"default"
argument_list|)
argument_list|,
name|instanceOf
argument_list|(
name|Lucene70Codec
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|codecService
operator|.
name|codec
argument_list|(
literal|"Lucene62"
argument_list|)
argument_list|,
name|instanceOf
argument_list|(
name|Lucene62Codec
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testDefault
specifier|public
name|void
name|testDefault
parameter_list|()
throws|throws
name|Exception
block|{
name|Codec
name|codec
init|=
name|createCodecService
argument_list|()
operator|.
name|codec
argument_list|(
literal|"default"
argument_list|)
decl_stmt|;
name|assertCompressionEquals
argument_list|(
name|Mode
operator|.
name|BEST_SPEED
argument_list|,
name|codec
argument_list|)
expr_stmt|;
block|}
DECL|method|testBestCompression
specifier|public
name|void
name|testBestCompression
parameter_list|()
throws|throws
name|Exception
block|{
name|Codec
name|codec
init|=
name|createCodecService
argument_list|()
operator|.
name|codec
argument_list|(
literal|"best_compression"
argument_list|)
decl_stmt|;
name|assertCompressionEquals
argument_list|(
name|Mode
operator|.
name|BEST_COMPRESSION
argument_list|,
name|codec
argument_list|)
expr_stmt|;
block|}
comment|// write some docs with it, inspect .si to see this was the used compression
DECL|method|assertCompressionEquals
specifier|private
name|void
name|assertCompressionEquals
parameter_list|(
name|Mode
name|expected
parameter_list|,
name|Codec
name|actual
parameter_list|)
throws|throws
name|Exception
block|{
name|Directory
name|dir
init|=
name|newDirectory
argument_list|()
decl_stmt|;
name|IndexWriterConfig
name|iwc
init|=
name|newIndexWriterConfig
argument_list|(
literal|null
argument_list|)
decl_stmt|;
name|iwc
operator|.
name|setCodec
argument_list|(
name|actual
argument_list|)
expr_stmt|;
name|IndexWriter
name|iw
init|=
operator|new
name|IndexWriter
argument_list|(
name|dir
argument_list|,
name|iwc
argument_list|)
decl_stmt|;
name|iw
operator|.
name|addDocument
argument_list|(
operator|new
name|Document
argument_list|()
argument_list|)
expr_stmt|;
name|iw
operator|.
name|commit
argument_list|()
expr_stmt|;
name|iw
operator|.
name|close
argument_list|()
expr_stmt|;
name|DirectoryReader
name|ir
init|=
name|DirectoryReader
operator|.
name|open
argument_list|(
name|dir
argument_list|)
decl_stmt|;
name|SegmentReader
name|sr
init|=
operator|(
name|SegmentReader
operator|)
name|ir
operator|.
name|leaves
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|reader
argument_list|()
decl_stmt|;
name|String
name|v
init|=
name|sr
operator|.
name|getSegmentInfo
argument_list|()
operator|.
name|info
operator|.
name|getAttribute
argument_list|(
name|Lucene50StoredFieldsFormat
operator|.
name|MODE_KEY
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
name|v
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expected
argument_list|,
name|Mode
operator|.
name|valueOf
argument_list|(
name|v
argument_list|)
argument_list|)
expr_stmt|;
name|ir
operator|.
name|close
argument_list|()
expr_stmt|;
name|dir
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
DECL|method|createCodecService
specifier|private
name|CodecService
name|createCodecService
parameter_list|()
throws|throws
name|IOException
block|{
name|Settings
name|nodeSettings
init|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|Environment
operator|.
name|PATH_HOME_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
name|createTempDir
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|IndexSettings
name|settings
init|=
name|IndexSettingsModule
operator|.
name|newIndexSettings
argument_list|(
literal|"_na"
argument_list|,
name|nodeSettings
argument_list|)
decl_stmt|;
name|SimilarityService
name|similarityService
init|=
operator|new
name|SimilarityService
argument_list|(
name|settings
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|)
decl_stmt|;
name|IndexAnalyzers
name|indexAnalyzers
init|=
name|createTestAnalysis
argument_list|(
name|settings
argument_list|,
name|nodeSettings
argument_list|)
operator|.
name|indexAnalyzers
decl_stmt|;
name|MapperRegistry
name|mapperRegistry
init|=
operator|new
name|MapperRegistry
argument_list|(
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|)
decl_stmt|;
name|MapperService
name|service
init|=
operator|new
name|MapperService
argument_list|(
name|settings
argument_list|,
name|indexAnalyzers
argument_list|,
name|xContentRegistry
argument_list|()
argument_list|,
name|similarityService
argument_list|,
name|mapperRegistry
argument_list|,
parameter_list|()
lambda|->
literal|null
argument_list|)
decl_stmt|;
return|return
operator|new
name|CodecService
argument_list|(
name|service
argument_list|,
name|ESLoggerFactory
operator|.
name|getLogger
argument_list|(
literal|"test"
argument_list|)
argument_list|)
return|;
block|}
block|}
end_class

end_unit

