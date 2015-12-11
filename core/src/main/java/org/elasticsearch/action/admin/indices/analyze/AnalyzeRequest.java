begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.admin.indices.analyze
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|admin
operator|.
name|indices
operator|.
name|analyze
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|Version
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
name|single
operator|.
name|shard
operator|.
name|SingleShardRequest
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

begin_comment
comment|/**  * A request to analyze a text associated with a specific index. Allow to provide  * the actual analyzer name to perform the analysis with.  */
end_comment

begin_class
DECL|class|AnalyzeRequest
specifier|public
class|class
name|AnalyzeRequest
extends|extends
name|SingleShardRequest
argument_list|<
name|AnalyzeRequest
argument_list|>
block|{
DECL|field|text
specifier|private
name|String
index|[]
name|text
decl_stmt|;
DECL|field|analyzer
specifier|private
name|String
name|analyzer
decl_stmt|;
DECL|field|tokenizer
specifier|private
name|String
name|tokenizer
decl_stmt|;
DECL|field|tokenFilters
specifier|private
name|String
index|[]
name|tokenFilters
init|=
name|Strings
operator|.
name|EMPTY_ARRAY
decl_stmt|;
DECL|field|charFilters
specifier|private
name|String
index|[]
name|charFilters
init|=
name|Strings
operator|.
name|EMPTY_ARRAY
decl_stmt|;
DECL|field|field
specifier|private
name|String
name|field
decl_stmt|;
DECL|field|explain
specifier|private
name|boolean
name|explain
init|=
literal|false
decl_stmt|;
DECL|field|attributes
specifier|private
name|String
index|[]
name|attributes
init|=
name|Strings
operator|.
name|EMPTY_ARRAY
decl_stmt|;
DECL|method|AnalyzeRequest
specifier|public
name|AnalyzeRequest
parameter_list|()
block|{     }
comment|/**      * Constructs a new analyzer request for the provided index.      *      * @param index The text to analyze      */
DECL|method|AnalyzeRequest
specifier|public
name|AnalyzeRequest
parameter_list|(
name|String
name|index
parameter_list|)
block|{
name|this
operator|.
name|index
argument_list|(
name|index
argument_list|)
expr_stmt|;
block|}
DECL|method|text
specifier|public
name|String
index|[]
name|text
parameter_list|()
block|{
return|return
name|this
operator|.
name|text
return|;
block|}
DECL|method|text
specifier|public
name|AnalyzeRequest
name|text
parameter_list|(
name|String
modifier|...
name|text
parameter_list|)
block|{
name|this
operator|.
name|text
operator|=
name|text
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|analyzer
specifier|public
name|AnalyzeRequest
name|analyzer
parameter_list|(
name|String
name|analyzer
parameter_list|)
block|{
name|this
operator|.
name|analyzer
operator|=
name|analyzer
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|analyzer
specifier|public
name|String
name|analyzer
parameter_list|()
block|{
return|return
name|this
operator|.
name|analyzer
return|;
block|}
DECL|method|tokenizer
specifier|public
name|AnalyzeRequest
name|tokenizer
parameter_list|(
name|String
name|tokenizer
parameter_list|)
block|{
name|this
operator|.
name|tokenizer
operator|=
name|tokenizer
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|tokenizer
specifier|public
name|String
name|tokenizer
parameter_list|()
block|{
return|return
name|this
operator|.
name|tokenizer
return|;
block|}
DECL|method|tokenFilters
specifier|public
name|AnalyzeRequest
name|tokenFilters
parameter_list|(
name|String
modifier|...
name|tokenFilters
parameter_list|)
block|{
if|if
condition|(
name|tokenFilters
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"token filters must not be null"
argument_list|)
throw|;
block|}
name|this
operator|.
name|tokenFilters
operator|=
name|tokenFilters
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|tokenFilters
specifier|public
name|String
index|[]
name|tokenFilters
parameter_list|()
block|{
return|return
name|this
operator|.
name|tokenFilters
return|;
block|}
DECL|method|charFilters
specifier|public
name|AnalyzeRequest
name|charFilters
parameter_list|(
name|String
modifier|...
name|charFilters
parameter_list|)
block|{
if|if
condition|(
name|charFilters
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"char filters must not be null"
argument_list|)
throw|;
block|}
name|this
operator|.
name|charFilters
operator|=
name|charFilters
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|charFilters
specifier|public
name|String
index|[]
name|charFilters
parameter_list|()
block|{
return|return
name|this
operator|.
name|charFilters
return|;
block|}
DECL|method|field
specifier|public
name|AnalyzeRequest
name|field
parameter_list|(
name|String
name|field
parameter_list|)
block|{
name|this
operator|.
name|field
operator|=
name|field
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|field
specifier|public
name|String
name|field
parameter_list|()
block|{
return|return
name|this
operator|.
name|field
return|;
block|}
DECL|method|explain
specifier|public
name|AnalyzeRequest
name|explain
parameter_list|(
name|boolean
name|explain
parameter_list|)
block|{
name|this
operator|.
name|explain
operator|=
name|explain
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|explain
specifier|public
name|boolean
name|explain
parameter_list|()
block|{
return|return
name|this
operator|.
name|explain
return|;
block|}
DECL|method|attributes
specifier|public
name|AnalyzeRequest
name|attributes
parameter_list|(
name|String
modifier|...
name|attributes
parameter_list|)
block|{
if|if
condition|(
name|attributes
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"attributes must not be null"
argument_list|)
throw|;
block|}
name|this
operator|.
name|attributes
operator|=
name|attributes
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|attributes
specifier|public
name|String
index|[]
name|attributes
parameter_list|()
block|{
return|return
name|this
operator|.
name|attributes
return|;
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
name|text
operator|==
literal|null
operator|||
name|text
operator|.
name|length
operator|==
literal|0
condition|)
block|{
name|validationException
operator|=
name|addValidationError
argument_list|(
literal|"text is missing"
argument_list|,
name|validationException
argument_list|)
expr_stmt|;
block|}
return|return
name|validationException
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
name|text
operator|=
name|in
operator|.
name|readStringArray
argument_list|()
expr_stmt|;
name|analyzer
operator|=
name|in
operator|.
name|readOptionalString
argument_list|()
expr_stmt|;
name|tokenizer
operator|=
name|in
operator|.
name|readOptionalString
argument_list|()
expr_stmt|;
name|tokenFilters
operator|=
name|in
operator|.
name|readStringArray
argument_list|()
expr_stmt|;
name|charFilters
operator|=
name|in
operator|.
name|readStringArray
argument_list|()
expr_stmt|;
name|field
operator|=
name|in
operator|.
name|readOptionalString
argument_list|()
expr_stmt|;
if|if
condition|(
name|in
operator|.
name|getVersion
argument_list|()
operator|.
name|onOrAfter
argument_list|(
name|Version
operator|.
name|V_2_2_0
argument_list|)
condition|)
block|{
name|explain
operator|=
name|in
operator|.
name|readBoolean
argument_list|()
expr_stmt|;
name|attributes
operator|=
name|in
operator|.
name|readStringArray
argument_list|()
expr_stmt|;
block|}
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
name|writeStringArray
argument_list|(
name|text
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeOptionalString
argument_list|(
name|analyzer
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeOptionalString
argument_list|(
name|tokenizer
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeStringArray
argument_list|(
name|tokenFilters
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeStringArray
argument_list|(
name|charFilters
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeOptionalString
argument_list|(
name|field
argument_list|)
expr_stmt|;
if|if
condition|(
name|out
operator|.
name|getVersion
argument_list|()
operator|.
name|onOrAfter
argument_list|(
name|Version
operator|.
name|V_2_2_0
argument_list|)
condition|)
block|{
name|out
operator|.
name|writeBoolean
argument_list|(
name|explain
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeStringArray
argument_list|(
name|attributes
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

