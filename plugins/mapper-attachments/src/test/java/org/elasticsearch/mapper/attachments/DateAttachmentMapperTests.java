begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.mapper.attachments
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|mapper
operator|.
name|attachments
package|;
end_package

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
name|DocumentMapper
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
name|DocumentMapperParser
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
name|core
operator|.
name|StringFieldMapper
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|mapper
operator|.
name|attachments
operator|.
name|AttachmentMapper
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Before
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|StreamsUtils
operator|.
name|copyToStringFromClasspath
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

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|DateAttachmentMapperTests
specifier|public
class|class
name|DateAttachmentMapperTests
extends|extends
name|AttachmentUnitTestCase
block|{
DECL|field|mapperParser
specifier|private
name|DocumentMapperParser
name|mapperParser
decl_stmt|;
annotation|@
name|Before
DECL|method|setupMapperParser
specifier|public
name|void
name|setupMapperParser
parameter_list|()
throws|throws
name|Exception
block|{
name|mapperParser
operator|=
name|MapperTestUtils
operator|.
name|newMapperService
argument_list|(
name|createTempDir
argument_list|()
argument_list|,
name|Settings
operator|.
name|EMPTY
argument_list|)
operator|.
name|documentMapperParser
argument_list|()
expr_stmt|;
name|mapperParser
operator|.
name|putTypeParser
argument_list|(
name|AttachmentMapper
operator|.
name|CONTENT_TYPE
argument_list|,
operator|new
name|AttachmentMapper
operator|.
name|TypeParser
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testSimpleMappings
specifier|public
name|void
name|testSimpleMappings
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|mapping
init|=
name|copyToStringFromClasspath
argument_list|(
literal|"/org/elasticsearch/index/mapper/attachment/test/unit/date/date-mapping.json"
argument_list|)
decl_stmt|;
name|DocumentMapper
name|docMapper
init|=
name|mapperParser
operator|.
name|parse
argument_list|(
name|mapping
argument_list|)
decl_stmt|;
comment|// Our mapping should be kept as a String
name|assertThat
argument_list|(
name|docMapper
operator|.
name|mappers
argument_list|()
operator|.
name|getMapper
argument_list|(
literal|"file.date"
argument_list|)
argument_list|,
name|instanceOf
argument_list|(
name|StringFieldMapper
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

