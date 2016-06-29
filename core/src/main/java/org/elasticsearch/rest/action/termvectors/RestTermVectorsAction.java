begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.rest.action.termvectors
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|rest
operator|.
name|action
operator|.
name|termvectors
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
name|termvectors
operator|.
name|TermVectorsRequest
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|termvectors
operator|.
name|TermVectorsResponse
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|client
operator|.
name|node
operator|.
name|NodeClient
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
name|Strings
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
name|inject
operator|.
name|Inject
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
name|common
operator|.
name|xcontent
operator|.
name|XContentFactory
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
name|xcontent
operator|.
name|XContentParser
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
name|VersionType
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|rest
operator|.
name|BaseRestHandler
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|rest
operator|.
name|RestChannel
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|rest
operator|.
name|RestController
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|rest
operator|.
name|RestRequest
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|rest
operator|.
name|action
operator|.
name|support
operator|.
name|RestActions
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|rest
operator|.
name|action
operator|.
name|support
operator|.
name|RestToXContentListener
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|rest
operator|.
name|RestRequest
operator|.
name|Method
operator|.
name|GET
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|rest
operator|.
name|RestRequest
operator|.
name|Method
operator|.
name|POST
import|;
end_import

begin_comment
comment|/**  * This class parses the json request and translates it into a  * TermVectorsRequest.  */
end_comment

begin_class
DECL|class|RestTermVectorsAction
specifier|public
class|class
name|RestTermVectorsAction
extends|extends
name|BaseRestHandler
block|{
annotation|@
name|Inject
DECL|method|RestTermVectorsAction
specifier|public
name|RestTermVectorsAction
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|RestController
name|controller
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|controller
operator|.
name|registerHandler
argument_list|(
name|GET
argument_list|,
literal|"/{index}/{type}/_termvectors"
argument_list|,
name|this
argument_list|)
expr_stmt|;
name|controller
operator|.
name|registerHandler
argument_list|(
name|POST
argument_list|,
literal|"/{index}/{type}/_termvectors"
argument_list|,
name|this
argument_list|)
expr_stmt|;
name|controller
operator|.
name|registerHandler
argument_list|(
name|GET
argument_list|,
literal|"/{index}/{type}/{id}/_termvectors"
argument_list|,
name|this
argument_list|)
expr_stmt|;
name|controller
operator|.
name|registerHandler
argument_list|(
name|POST
argument_list|,
literal|"/{index}/{type}/{id}/_termvectors"
argument_list|,
name|this
argument_list|)
expr_stmt|;
comment|// we keep usage of _termvector as alias for now
name|controller
operator|.
name|registerHandler
argument_list|(
name|GET
argument_list|,
literal|"/{index}/{type}/_termvector"
argument_list|,
name|this
argument_list|)
expr_stmt|;
name|controller
operator|.
name|registerHandler
argument_list|(
name|POST
argument_list|,
literal|"/{index}/{type}/_termvector"
argument_list|,
name|this
argument_list|)
expr_stmt|;
name|controller
operator|.
name|registerHandler
argument_list|(
name|GET
argument_list|,
literal|"/{index}/{type}/{id}/_termvector"
argument_list|,
name|this
argument_list|)
expr_stmt|;
name|controller
operator|.
name|registerHandler
argument_list|(
name|POST
argument_list|,
literal|"/{index}/{type}/{id}/_termvector"
argument_list|,
name|this
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|handleRequest
specifier|public
name|void
name|handleRequest
parameter_list|(
specifier|final
name|RestRequest
name|request
parameter_list|,
specifier|final
name|RestChannel
name|channel
parameter_list|,
specifier|final
name|NodeClient
name|client
parameter_list|)
throws|throws
name|Exception
block|{
name|TermVectorsRequest
name|termVectorsRequest
init|=
operator|new
name|TermVectorsRequest
argument_list|(
name|request
operator|.
name|param
argument_list|(
literal|"index"
argument_list|)
argument_list|,
name|request
operator|.
name|param
argument_list|(
literal|"type"
argument_list|)
argument_list|,
name|request
operator|.
name|param
argument_list|(
literal|"id"
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|RestActions
operator|.
name|hasBodyContent
argument_list|(
name|request
argument_list|)
condition|)
block|{
try|try
init|(
name|XContentParser
name|parser
init|=
name|XContentFactory
operator|.
name|xContent
argument_list|(
name|RestActions
operator|.
name|guessBodyContentType
argument_list|(
name|request
argument_list|)
argument_list|)
operator|.
name|createParser
argument_list|(
name|RestActions
operator|.
name|getRestContent
argument_list|(
name|request
argument_list|)
argument_list|)
init|)
block|{
name|TermVectorsRequest
operator|.
name|parseRequest
argument_list|(
name|termVectorsRequest
argument_list|,
name|parser
argument_list|)
expr_stmt|;
block|}
block|}
name|readURIParameters
argument_list|(
name|termVectorsRequest
argument_list|,
name|request
argument_list|)
expr_stmt|;
name|client
operator|.
name|termVectors
argument_list|(
name|termVectorsRequest
argument_list|,
operator|new
name|RestToXContentListener
argument_list|<
name|TermVectorsResponse
argument_list|>
argument_list|(
name|channel
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|readURIParameters
specifier|static
specifier|public
name|void
name|readURIParameters
parameter_list|(
name|TermVectorsRequest
name|termVectorsRequest
parameter_list|,
name|RestRequest
name|request
parameter_list|)
block|{
name|String
name|fields
init|=
name|request
operator|.
name|param
argument_list|(
literal|"fields"
argument_list|)
decl_stmt|;
name|addFieldStringsFromParameter
argument_list|(
name|termVectorsRequest
argument_list|,
name|fields
argument_list|)
expr_stmt|;
name|termVectorsRequest
operator|.
name|offsets
argument_list|(
name|request
operator|.
name|paramAsBoolean
argument_list|(
literal|"offsets"
argument_list|,
name|termVectorsRequest
operator|.
name|offsets
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|termVectorsRequest
operator|.
name|positions
argument_list|(
name|request
operator|.
name|paramAsBoolean
argument_list|(
literal|"positions"
argument_list|,
name|termVectorsRequest
operator|.
name|positions
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|termVectorsRequest
operator|.
name|payloads
argument_list|(
name|request
operator|.
name|paramAsBoolean
argument_list|(
literal|"payloads"
argument_list|,
name|termVectorsRequest
operator|.
name|payloads
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|termVectorsRequest
operator|.
name|routing
argument_list|(
name|request
operator|.
name|param
argument_list|(
literal|"routing"
argument_list|)
argument_list|)
expr_stmt|;
name|termVectorsRequest
operator|.
name|realtime
argument_list|(
name|request
operator|.
name|paramAsBoolean
argument_list|(
literal|"realtime"
argument_list|,
name|termVectorsRequest
operator|.
name|realtime
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|termVectorsRequest
operator|.
name|version
argument_list|(
name|RestActions
operator|.
name|parseVersion
argument_list|(
name|request
argument_list|,
name|termVectorsRequest
operator|.
name|version
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|termVectorsRequest
operator|.
name|versionType
argument_list|(
name|VersionType
operator|.
name|fromString
argument_list|(
name|request
operator|.
name|param
argument_list|(
literal|"version_type"
argument_list|)
argument_list|,
name|termVectorsRequest
operator|.
name|versionType
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|termVectorsRequest
operator|.
name|parent
argument_list|(
name|request
operator|.
name|param
argument_list|(
literal|"parent"
argument_list|)
argument_list|)
expr_stmt|;
name|termVectorsRequest
operator|.
name|preference
argument_list|(
name|request
operator|.
name|param
argument_list|(
literal|"preference"
argument_list|)
argument_list|)
expr_stmt|;
name|termVectorsRequest
operator|.
name|termStatistics
argument_list|(
name|request
operator|.
name|paramAsBoolean
argument_list|(
literal|"termStatistics"
argument_list|,
name|termVectorsRequest
operator|.
name|termStatistics
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|termVectorsRequest
operator|.
name|termStatistics
argument_list|(
name|request
operator|.
name|paramAsBoolean
argument_list|(
literal|"term_statistics"
argument_list|,
name|termVectorsRequest
operator|.
name|termStatistics
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|termVectorsRequest
operator|.
name|fieldStatistics
argument_list|(
name|request
operator|.
name|paramAsBoolean
argument_list|(
literal|"fieldStatistics"
argument_list|,
name|termVectorsRequest
operator|.
name|fieldStatistics
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|termVectorsRequest
operator|.
name|fieldStatistics
argument_list|(
name|request
operator|.
name|paramAsBoolean
argument_list|(
literal|"field_statistics"
argument_list|,
name|termVectorsRequest
operator|.
name|fieldStatistics
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|addFieldStringsFromParameter
specifier|static
specifier|public
name|void
name|addFieldStringsFromParameter
parameter_list|(
name|TermVectorsRequest
name|termVectorsRequest
parameter_list|,
name|String
name|fields
parameter_list|)
block|{
name|Set
argument_list|<
name|String
argument_list|>
name|selectedFields
init|=
name|termVectorsRequest
operator|.
name|selectedFields
argument_list|()
decl_stmt|;
if|if
condition|(
name|fields
operator|!=
literal|null
condition|)
block|{
name|String
index|[]
name|paramFieldStrings
init|=
name|Strings
operator|.
name|commaDelimitedListToStringArray
argument_list|(
name|fields
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|field
range|:
name|paramFieldStrings
control|)
block|{
if|if
condition|(
name|selectedFields
operator|==
literal|null
condition|)
block|{
name|selectedFields
operator|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|selectedFields
operator|.
name|contains
argument_list|(
name|field
argument_list|)
condition|)
block|{
name|field
operator|=
name|field
operator|.
name|replaceAll
argument_list|(
literal|"\\s"
argument_list|,
literal|""
argument_list|)
expr_stmt|;
name|selectedFields
operator|.
name|add
argument_list|(
name|field
argument_list|)
expr_stmt|;
block|}
block|}
block|}
if|if
condition|(
name|selectedFields
operator|!=
literal|null
condition|)
block|{
name|termVectorsRequest
operator|.
name|selectedFields
argument_list|(
name|selectedFields
operator|.
name|toArray
argument_list|(
operator|new
name|String
index|[
name|selectedFields
operator|.
name|size
argument_list|()
index|]
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

