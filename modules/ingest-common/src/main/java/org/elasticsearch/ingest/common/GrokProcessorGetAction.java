begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.ingest.common
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|ingest
operator|.
name|common
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
name|Action
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
name|ActionListener
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
name|ActionRequest
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
name|ActionRequestBuilder
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
name|ActionRequestValidationException
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
name|support
operator|.
name|ActionFilters
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
name|support
operator|.
name|HandledTransportAction
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
name|support
operator|.
name|master
operator|.
name|AcknowledgedResponse
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
name|ElasticsearchClient
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
name|cluster
operator|.
name|metadata
operator|.
name|IndexNameExpressionResolver
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
name|io
operator|.
name|stream
operator|.
name|StreamInput
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
name|io
operator|.
name|stream
operator|.
name|StreamOutput
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
name|ToXContent
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
name|ToXContentObject
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
name|XContentBuilder
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
name|BytesRestResponse
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
name|RestResponse
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
name|RestBuilderListener
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|threadpool
operator|.
name|ThreadPool
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|transport
operator|.
name|TransportService
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
name|Map
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|ingest
operator|.
name|common
operator|.
name|IngestCommonPlugin
operator|.
name|GROK_PATTERNS
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
name|RestStatus
operator|.
name|OK
import|;
end_import

begin_class
DECL|class|GrokProcessorGetAction
specifier|public
class|class
name|GrokProcessorGetAction
extends|extends
name|Action
argument_list|<
name|GrokProcessorGetAction
operator|.
name|Request
argument_list|,
name|GrokProcessorGetAction
operator|.
name|Response
argument_list|,
name|GrokProcessorGetAction
operator|.
name|RequestBuilder
argument_list|>
block|{
DECL|field|INSTANCE
specifier|public
specifier|static
specifier|final
name|GrokProcessorGetAction
name|INSTANCE
init|=
operator|new
name|GrokProcessorGetAction
argument_list|()
decl_stmt|;
DECL|field|NAME
specifier|public
specifier|static
specifier|final
name|String
name|NAME
init|=
literal|"cluster:admin/ingest/processor/grok/get"
decl_stmt|;
DECL|method|GrokProcessorGetAction
specifier|private
name|GrokProcessorGetAction
parameter_list|()
block|{
name|super
argument_list|(
name|NAME
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|newRequestBuilder
specifier|public
name|RequestBuilder
name|newRequestBuilder
parameter_list|(
name|ElasticsearchClient
name|client
parameter_list|)
block|{
return|return
operator|new
name|RequestBuilder
argument_list|(
name|client
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|newResponse
specifier|public
name|Response
name|newResponse
parameter_list|()
block|{
return|return
operator|new
name|Response
argument_list|(
literal|null
argument_list|)
return|;
block|}
DECL|class|Request
specifier|public
specifier|static
class|class
name|Request
extends|extends
name|ActionRequest
block|{
annotation|@
name|Override
DECL|method|validate
specifier|public
name|ActionRequestValidationException
name|validate
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
block|}
DECL|class|RequestBuilder
specifier|public
specifier|static
class|class
name|RequestBuilder
extends|extends
name|ActionRequestBuilder
argument_list|<
name|Request
argument_list|,
name|Response
argument_list|,
name|RequestBuilder
argument_list|>
block|{
DECL|method|RequestBuilder
specifier|public
name|RequestBuilder
parameter_list|(
name|ElasticsearchClient
name|client
parameter_list|)
block|{
name|super
argument_list|(
name|client
argument_list|,
name|GrokProcessorGetAction
operator|.
name|INSTANCE
argument_list|,
operator|new
name|Request
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
DECL|class|Response
specifier|public
specifier|static
class|class
name|Response
extends|extends
name|AcknowledgedResponse
implements|implements
name|ToXContentObject
block|{
DECL|field|grokPatterns
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|grokPatterns
decl_stmt|;
DECL|method|Response
specifier|public
name|Response
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|grokPatterns
parameter_list|)
block|{
name|this
operator|.
name|grokPatterns
operator|=
name|grokPatterns
expr_stmt|;
block|}
DECL|method|getGrokPatterns
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|getGrokPatterns
parameter_list|()
block|{
return|return
name|grokPatterns
return|;
block|}
annotation|@
name|Override
DECL|method|toXContent
specifier|public
name|XContentBuilder
name|toXContent
parameter_list|(
name|XContentBuilder
name|builder
parameter_list|,
name|Params
name|params
parameter_list|)
throws|throws
name|IOException
block|{
name|builder
operator|.
name|startObject
argument_list|()
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"patterns"
argument_list|)
expr_stmt|;
name|builder
operator|.
name|map
argument_list|(
name|grokPatterns
argument_list|)
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
return|return
name|builder
return|;
block|}
annotation|@
name|Override
DECL|method|readFrom
specifier|public
name|void
name|readFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|grokPatterns
operator|=
name|in
operator|.
name|readMap
argument_list|(
name|StreamInput
operator|::
name|readString
argument_list|,
name|StreamInput
operator|::
name|readString
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|writeTo
specifier|public
name|void
name|writeTo
parameter_list|(
name|StreamOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeMap
argument_list|(
name|grokPatterns
argument_list|,
name|StreamOutput
operator|::
name|writeString
argument_list|,
name|StreamOutput
operator|::
name|writeString
argument_list|)
expr_stmt|;
block|}
block|}
DECL|class|TransportAction
specifier|public
specifier|static
class|class
name|TransportAction
extends|extends
name|HandledTransportAction
argument_list|<
name|Request
argument_list|,
name|Response
argument_list|>
block|{
annotation|@
name|Inject
DECL|method|TransportAction
specifier|public
name|TransportAction
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|ThreadPool
name|threadPool
parameter_list|,
name|TransportService
name|transportService
parameter_list|,
name|ActionFilters
name|actionFilters
parameter_list|,
name|IndexNameExpressionResolver
name|indexNameExpressionResolver
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|,
name|NAME
argument_list|,
name|threadPool
argument_list|,
name|transportService
argument_list|,
name|actionFilters
argument_list|,
name|indexNameExpressionResolver
argument_list|,
name|Request
operator|::
operator|new
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|doExecute
specifier|protected
name|void
name|doExecute
parameter_list|(
name|Request
name|request
parameter_list|,
name|ActionListener
argument_list|<
name|Response
argument_list|>
name|listener
parameter_list|)
block|{
try|try
block|{
name|listener
operator|.
name|onResponse
argument_list|(
operator|new
name|Response
argument_list|(
name|GROK_PATTERNS
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|listener
operator|.
name|onFailure
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|class|RestAction
specifier|public
specifier|static
class|class
name|RestAction
extends|extends
name|BaseRestHandler
block|{
DECL|method|RestAction
specifier|public
name|RestAction
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
literal|"/_ingest/processor/grok"
argument_list|,
name|this
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|getName
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
literal|"ingest_processor_grok_get"
return|;
block|}
annotation|@
name|Override
DECL|method|prepareRequest
specifier|protected
name|RestChannelConsumer
name|prepareRequest
parameter_list|(
name|RestRequest
name|request
parameter_list|,
name|NodeClient
name|client
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|channel
lambda|->
name|client
operator|.
name|executeLocally
argument_list|(
name|INSTANCE
argument_list|,
operator|new
name|Request
argument_list|()
argument_list|,
operator|new
name|RestBuilderListener
argument_list|<
name|Response
argument_list|>
argument_list|(
name|channel
argument_list|)
block|{
block|@Override                 public RestResponse buildResponse(Response response
operator|,
name|XContentBuilder
name|builder
block|)
throws|throws
name|Exception
block|{
name|response
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|ToXContent
operator|.
name|EMPTY_PARAMS
argument_list|)
expr_stmt|;
return|return
operator|new
name|BytesRestResponse
argument_list|(
name|OK
argument_list|,
name|builder
argument_list|)
return|;
block|}
block|}
block|)
class|;
end_class

unit|}     } }
end_unit
