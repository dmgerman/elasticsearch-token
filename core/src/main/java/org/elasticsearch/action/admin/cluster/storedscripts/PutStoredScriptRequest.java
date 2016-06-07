begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.admin.cluster.storedscripts
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|admin
operator|.
name|cluster
operator|.
name|storedscripts
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
name|master
operator|.
name|AcknowledgedRequest
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
name|bytes
operator|.
name|BytesReference
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
name|xcontent
operator|.
name|XContentHelper
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
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|ValidateActions
operator|.
name|addValidationError
import|;
end_import

begin_class
DECL|class|PutStoredScriptRequest
specifier|public
class|class
name|PutStoredScriptRequest
extends|extends
name|AcknowledgedRequest
argument_list|<
name|PutStoredScriptRequest
argument_list|>
block|{
DECL|field|id
specifier|private
name|String
name|id
decl_stmt|;
DECL|field|scriptLang
specifier|private
name|String
name|scriptLang
decl_stmt|;
DECL|field|script
specifier|private
name|BytesReference
name|script
decl_stmt|;
DECL|method|PutStoredScriptRequest
specifier|public
name|PutStoredScriptRequest
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
DECL|method|PutStoredScriptRequest
specifier|public
name|PutStoredScriptRequest
parameter_list|(
name|String
name|scriptLang
parameter_list|)
block|{
name|super
argument_list|()
expr_stmt|;
name|this
operator|.
name|scriptLang
operator|=
name|scriptLang
expr_stmt|;
block|}
DECL|method|PutStoredScriptRequest
specifier|public
name|PutStoredScriptRequest
parameter_list|(
name|String
name|scriptLang
parameter_list|,
name|String
name|id
parameter_list|)
block|{
name|super
argument_list|()
expr_stmt|;
name|this
operator|.
name|scriptLang
operator|=
name|scriptLang
expr_stmt|;
name|this
operator|.
name|id
operator|=
name|id
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|validate
specifier|public
name|ActionRequestValidationException
name|validate
parameter_list|()
block|{
name|ActionRequestValidationException
name|validationException
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|id
operator|==
literal|null
condition|)
block|{
name|validationException
operator|=
name|addValidationError
argument_list|(
literal|"id is missing"
argument_list|,
name|validationException
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|id
operator|.
name|contains
argument_list|(
literal|"#"
argument_list|)
condition|)
block|{
name|validationException
operator|=
name|addValidationError
argument_list|(
literal|"id can't contain: '#'"
argument_list|,
name|validationException
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|scriptLang
operator|==
literal|null
condition|)
block|{
name|validationException
operator|=
name|addValidationError
argument_list|(
literal|"lang is missing"
argument_list|,
name|validationException
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|scriptLang
operator|.
name|contains
argument_list|(
literal|"#"
argument_list|)
condition|)
block|{
name|validationException
operator|=
name|addValidationError
argument_list|(
literal|"lang can't contain: '#'"
argument_list|,
name|validationException
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|script
operator|==
literal|null
condition|)
block|{
name|validationException
operator|=
name|addValidationError
argument_list|(
literal|"script is missing"
argument_list|,
name|validationException
argument_list|)
expr_stmt|;
block|}
return|return
name|validationException
return|;
block|}
DECL|method|scriptLang
specifier|public
name|String
name|scriptLang
parameter_list|()
block|{
return|return
name|scriptLang
return|;
block|}
DECL|method|scriptLang
specifier|public
name|PutStoredScriptRequest
name|scriptLang
parameter_list|(
name|String
name|scriptLang
parameter_list|)
block|{
name|this
operator|.
name|scriptLang
operator|=
name|scriptLang
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|id
specifier|public
name|String
name|id
parameter_list|()
block|{
return|return
name|id
return|;
block|}
DECL|method|id
specifier|public
name|PutStoredScriptRequest
name|id
parameter_list|(
name|String
name|id
parameter_list|)
block|{
name|this
operator|.
name|id
operator|=
name|id
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|script
specifier|public
name|BytesReference
name|script
parameter_list|()
block|{
return|return
name|script
return|;
block|}
DECL|method|script
specifier|public
name|PutStoredScriptRequest
name|script
parameter_list|(
name|BytesReference
name|source
parameter_list|)
block|{
name|this
operator|.
name|script
operator|=
name|source
expr_stmt|;
return|return
name|this
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
name|scriptLang
operator|=
name|in
operator|.
name|readString
argument_list|()
expr_stmt|;
name|id
operator|=
name|in
operator|.
name|readOptionalString
argument_list|()
expr_stmt|;
name|script
operator|=
name|in
operator|.
name|readBytesReference
argument_list|()
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
name|writeString
argument_list|(
name|scriptLang
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeOptionalString
argument_list|(
name|id
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeBytesReference
argument_list|(
name|script
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|toString
specifier|public
name|String
name|toString
parameter_list|()
block|{
name|String
name|sSource
init|=
literal|"_na_"
decl_stmt|;
try|try
block|{
name|sSource
operator|=
name|XContentHelper
operator|.
name|convertToJson
argument_list|(
name|script
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
comment|// ignore
block|}
return|return
literal|"put script {["
operator|+
name|id
operator|+
literal|"]["
operator|+
name|scriptLang
operator|+
literal|"], script["
operator|+
name|sSource
operator|+
literal|"]}"
return|;
block|}
block|}
end_class

end_unit
