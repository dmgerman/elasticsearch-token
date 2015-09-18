begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch
package|package
name|org
operator|.
name|elasticsearch
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
comment|/**  * Generic security exception  */
end_comment

begin_class
DECL|class|ElasticsearchSecurityException
specifier|public
class|class
name|ElasticsearchSecurityException
extends|extends
name|ElasticsearchException
block|{
DECL|field|status
specifier|private
specifier|final
name|RestStatus
name|status
decl_stmt|;
DECL|method|ElasticsearchSecurityException
specifier|public
name|ElasticsearchSecurityException
parameter_list|(
name|String
name|msg
parameter_list|,
name|RestStatus
name|status
parameter_list|,
name|Throwable
name|cause
parameter_list|,
name|Object
modifier|...
name|args
parameter_list|)
block|{
name|super
argument_list|(
name|msg
argument_list|,
name|cause
argument_list|,
name|args
argument_list|)
expr_stmt|;
name|this
operator|.
name|status
operator|=
name|status
expr_stmt|;
block|}
DECL|method|ElasticsearchSecurityException
specifier|public
name|ElasticsearchSecurityException
parameter_list|(
name|String
name|msg
parameter_list|,
name|Throwable
name|cause
parameter_list|,
name|Object
modifier|...
name|args
parameter_list|)
block|{
name|this
argument_list|(
name|msg
argument_list|,
name|ExceptionsHelper
operator|.
name|status
argument_list|(
name|cause
argument_list|)
argument_list|,
name|cause
argument_list|,
name|args
argument_list|)
expr_stmt|;
block|}
DECL|method|ElasticsearchSecurityException
specifier|public
name|ElasticsearchSecurityException
parameter_list|(
name|String
name|msg
parameter_list|,
name|Object
modifier|...
name|args
parameter_list|)
block|{
name|this
argument_list|(
name|msg
argument_list|,
name|RestStatus
operator|.
name|INTERNAL_SERVER_ERROR
argument_list|,
literal|null
argument_list|,
name|args
argument_list|)
expr_stmt|;
block|}
DECL|method|ElasticsearchSecurityException
specifier|public
name|ElasticsearchSecurityException
parameter_list|(
name|String
name|msg
parameter_list|,
name|RestStatus
name|status
parameter_list|,
name|Object
modifier|...
name|args
parameter_list|)
block|{
name|this
argument_list|(
name|msg
argument_list|,
name|status
argument_list|,
literal|null
argument_list|,
name|args
argument_list|)
expr_stmt|;
block|}
DECL|method|ElasticsearchSecurityException
specifier|public
name|ElasticsearchSecurityException
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|status
operator|=
name|RestStatus
operator|.
name|readFrom
argument_list|(
name|in
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
name|RestStatus
operator|.
name|writeTo
argument_list|(
name|out
argument_list|,
name|status
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|status
specifier|public
specifier|final
name|RestStatus
name|status
parameter_list|()
block|{
return|return
name|status
return|;
block|}
block|}
end_class

end_unit
