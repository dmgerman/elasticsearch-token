begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.mapper.attachment.test.integration
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|mapper
operator|.
name|attachment
operator|.
name|test
operator|.
name|integration
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|count
operator|.
name|CountResponse
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
name|index
operator|.
name|mapper
operator|.
name|MapperParsingException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|plugins
operator|.
name|PluginsService
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
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
name|Slow
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|client
operator|.
name|Requests
operator|.
name|putMappingRequest
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|io
operator|.
name|Streams
operator|.
name|copyToBytesFromClasspath
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|io
operator|.
name|Streams
operator|.
name|copyToStringFromClasspath
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|settings
operator|.
name|Settings
operator|.
name|settingsBuilder
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|xcontent
operator|.
name|XContentFactory
operator|.
name|jsonBuilder
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|query
operator|.
name|QueryBuilders
operator|.
name|queryStringQuery
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
name|equalTo
import|;
end_import

begin_comment
comment|/**  * Test case for issue https://github.com/elasticsearch/elasticsearch-mapper-attachments/issues/18  */
end_comment

begin_class
annotation|@
name|Slow
DECL|class|EncryptedAttachmentIntegrationTests
specifier|public
class|class
name|EncryptedAttachmentIntegrationTests
extends|extends
name|AttachmentIntegrationTestCase
block|{
DECL|field|ignore_errors
specifier|private
name|boolean
name|ignore_errors
init|=
literal|true
decl_stmt|;
annotation|@
name|Override
DECL|method|nodeSettings
specifier|protected
name|Settings
name|nodeSettings
parameter_list|(
name|int
name|nodeOrdinal
parameter_list|)
block|{
return|return
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|super
operator|.
name|nodeSettings
argument_list|(
name|nodeOrdinal
argument_list|)
argument_list|)
operator|.
name|put
argument_list|(
literal|"plugins."
operator|+
name|PluginsService
operator|.
name|LOAD_PLUGIN_FROM_CLASSPATH
argument_list|,
literal|true
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|indexSettings
specifier|public
name|Settings
name|indexSettings
parameter_list|()
block|{
return|return
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"index.numberOfReplicas"
argument_list|,
literal|0
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.mapping.attachment.ignore_errors"
argument_list|,
name|ignore_errors
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
comment|/**      * When we want to ignore errors (default)      */
annotation|@
name|Test
DECL|method|testMultipleAttachmentsWithEncryptedDoc
specifier|public
name|void
name|testMultipleAttachmentsWithEncryptedDoc
parameter_list|()
throws|throws
name|Exception
block|{
name|ignore_errors
operator|=
literal|true
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"creating index [test]"
argument_list|)
expr_stmt|;
name|createIndex
argument_list|(
literal|"test"
argument_list|)
expr_stmt|;
name|String
name|mapping
init|=
name|copyToStringFromClasspath
argument_list|(
literal|"/org/elasticsearch/index/mapper/attachment/test/integration/encrypted/test-mapping.json"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|html
init|=
name|copyToBytesFromClasspath
argument_list|(
literal|"/org/elasticsearch/index/mapper/attachment/test/sample-files/htmlWithValidDateMeta.html"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|pdf
init|=
name|copyToBytesFromClasspath
argument_list|(
literal|"/org/elasticsearch/index/mapper/attachment/test/sample-files/encrypted.pdf"
argument_list|)
decl_stmt|;
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|putMapping
argument_list|(
name|putMappingRequest
argument_list|(
literal|"test"
argument_list|)
operator|.
name|type
argument_list|(
literal|"person"
argument_list|)
operator|.
name|source
argument_list|(
name|mapping
argument_list|)
argument_list|)
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|index
argument_list|(
literal|"test"
argument_list|,
literal|"person"
argument_list|,
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"file1"
argument_list|,
name|html
argument_list|)
operator|.
name|field
argument_list|(
literal|"file2"
argument_list|,
name|pdf
argument_list|)
operator|.
name|field
argument_list|(
literal|"hello"
argument_list|,
literal|"world"
argument_list|)
operator|.
name|endObject
argument_list|()
argument_list|)
expr_stmt|;
name|refresh
argument_list|()
expr_stmt|;
name|CountResponse
name|countResponse
init|=
name|client
argument_list|()
operator|.
name|prepareCount
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setQuery
argument_list|(
name|queryStringQuery
argument_list|(
literal|"World"
argument_list|)
operator|.
name|defaultField
argument_list|(
literal|"file1.content"
argument_list|)
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertThatWithError
argument_list|(
name|countResponse
operator|.
name|getCount
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1l
argument_list|)
argument_list|)
expr_stmt|;
name|countResponse
operator|=
name|client
argument_list|()
operator|.
name|prepareCount
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setQuery
argument_list|(
name|queryStringQuery
argument_list|(
literal|"World"
argument_list|)
operator|.
name|defaultField
argument_list|(
literal|"hello"
argument_list|)
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|get
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|countResponse
operator|.
name|getCount
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1l
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**      * When we don't want to ignore errors      */
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|MapperParsingException
operator|.
name|class
argument_list|)
DECL|method|testMultipleAttachmentsWithEncryptedDocNotIgnoringErrors
specifier|public
name|void
name|testMultipleAttachmentsWithEncryptedDocNotIgnoringErrors
parameter_list|()
throws|throws
name|Exception
block|{
name|ignore_errors
operator|=
literal|false
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"creating index [test]"
argument_list|)
expr_stmt|;
name|createIndex
argument_list|(
literal|"test"
argument_list|)
expr_stmt|;
name|String
name|mapping
init|=
name|copyToStringFromClasspath
argument_list|(
literal|"/org/elasticsearch/index/mapper/attachment/test/integration/encrypted/test-mapping.json"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|html
init|=
name|copyToBytesFromClasspath
argument_list|(
literal|"/org/elasticsearch/index/mapper/attachment/test/sample-files/htmlWithValidDateMeta.html"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|pdf
init|=
name|copyToBytesFromClasspath
argument_list|(
literal|"/org/elasticsearch/index/mapper/attachment/test/sample-files/encrypted.pdf"
argument_list|)
decl_stmt|;
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|putMapping
argument_list|(
name|putMappingRequest
argument_list|(
literal|"test"
argument_list|)
operator|.
name|type
argument_list|(
literal|"person"
argument_list|)
operator|.
name|source
argument_list|(
name|mapping
argument_list|)
argument_list|)
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|index
argument_list|(
literal|"test"
argument_list|,
literal|"person"
argument_list|,
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"file1"
argument_list|,
name|html
argument_list|)
operator|.
name|field
argument_list|(
literal|"file2"
argument_list|,
name|pdf
argument_list|)
operator|.
name|field
argument_list|(
literal|"hello"
argument_list|,
literal|"world"
argument_list|)
operator|.
name|endObject
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

