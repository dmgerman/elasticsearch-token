begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.client.documentation
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|client
operator|.
name|documentation
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticsearchException
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
name|DocWriteResponse
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
name|delete
operator|.
name|DeleteRequest
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
name|delete
operator|.
name|DeleteResponse
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
name|WriteRequest
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
name|ESRestHighLevelClientTestCase
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
name|RestHighLevelClient
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
name|unit
operator|.
name|TimeValue
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
name|RestStatus
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

begin_comment
comment|/**  * This class is used to generate the Java Delete API documentation.  * You need to wrap your code between two tags like:  * // tag::example[]  * // end::example[]  *  * Where example is your tag name.  *  * Then in the documentation, you can extract what is between tag and end tags with  * ["source","java",subs="attributes,callouts"]  * --------------------------------------------------  * sys2::[perl -ne 'exit if /end::example/; print if $tag; $tag = $tag || /tag::example/' {docdir}/../../client/rest-high-level/src/test/java/org/elasticsearch/client/documentation/DeleteDocumentationIT.java]  * --------------------------------------------------  */
end_comment

begin_class
DECL|class|DeleteDocumentationIT
specifier|public
class|class
name|DeleteDocumentationIT
extends|extends
name|ESRestHighLevelClientTestCase
block|{
comment|/**      * This test documents docs/java-rest/high-level/document/delete.asciidoc      */
DECL|method|testDelete
specifier|public
name|void
name|testDelete
parameter_list|()
throws|throws
name|IOException
block|{
name|RestHighLevelClient
name|client
init|=
name|highLevelClient
argument_list|()
decl_stmt|;
comment|// tag::delete-request[]
name|DeleteRequest
name|request
init|=
operator|new
name|DeleteRequest
argument_list|(
literal|"index"
argument_list|,
comment|//<1>
literal|"type"
argument_list|,
comment|//<2>
literal|"id"
argument_list|)
decl_stmt|;
comment|//<3>
comment|// end::delete-request[]
comment|// tag::delete-request-props[]
name|request
operator|.
name|timeout
argument_list|(
name|TimeValue
operator|.
name|timeValueSeconds
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
comment|//<1>
name|request
operator|.
name|timeout
argument_list|(
literal|"1s"
argument_list|)
expr_stmt|;
comment|//<2>
name|request
operator|.
name|setRefreshPolicy
argument_list|(
name|WriteRequest
operator|.
name|RefreshPolicy
operator|.
name|WAIT_UNTIL
argument_list|)
expr_stmt|;
comment|//<3>
name|request
operator|.
name|setRefreshPolicy
argument_list|(
literal|"wait_for"
argument_list|)
expr_stmt|;
comment|//<4>
name|request
operator|.
name|version
argument_list|(
literal|2
argument_list|)
expr_stmt|;
comment|//<5>
name|request
operator|.
name|versionType
argument_list|(
name|VersionType
operator|.
name|EXTERNAL
argument_list|)
expr_stmt|;
comment|//<6>
comment|// end::delete-request-props[]
comment|// tag::delete-execute[]
name|DeleteResponse
name|response
init|=
name|client
operator|.
name|delete
argument_list|(
name|request
argument_list|)
decl_stmt|;
comment|// end::delete-execute[]
try|try
block|{
comment|// tag::delete-notfound[]
if|if
condition|(
name|response
operator|.
name|getResult
argument_list|()
operator|.
name|equals
argument_list|(
name|DocWriteResponse
operator|.
name|Result
operator|.
name|NOT_FOUND
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|Exception
argument_list|(
literal|"Can't find document to be removed"
argument_list|)
throw|;
comment|//<1>
block|}
comment|// end::delete-notfound[]
block|}
catch|catch
parameter_list|(
name|Exception
name|ignored
parameter_list|)
block|{ }
comment|// tag::delete-execute-async[]
name|client
operator|.
name|deleteAsync
argument_list|(
name|request
argument_list|,
operator|new
name|ActionListener
argument_list|<
name|DeleteResponse
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onResponse
parameter_list|(
name|DeleteResponse
name|deleteResponse
parameter_list|)
block|{
comment|//<1>
block|}
annotation|@
name|Override
specifier|public
name|void
name|onFailure
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
comment|//<2>
block|}
block|}
argument_list|)
expr_stmt|;
comment|// end::delete-execute-async[]
comment|// tag::delete-conflict[]
try|try
block|{
name|client
operator|.
name|delete
argument_list|(
name|request
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ElasticsearchException
name|exception
parameter_list|)
block|{
if|if
condition|(
name|exception
operator|.
name|status
argument_list|()
operator|.
name|equals
argument_list|(
name|RestStatus
operator|.
name|CONFLICT
argument_list|)
condition|)
block|{
comment|//<1>
block|}
block|}
comment|// end::delete-conflict[]
block|}
block|}
end_class

end_unit

