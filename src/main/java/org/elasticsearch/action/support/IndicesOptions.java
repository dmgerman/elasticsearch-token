begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.support
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|support
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticsearchIllegalArgumentException
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
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_comment
comment|/**  * Controls how to deal when concrete indices are unavailable (closed& missing), to what wildcard expression expand  * (all, closed or open indices) and how to deal when a wildcard expression resolves into no concrete indices.  */
end_comment

begin_class
DECL|class|IndicesOptions
specifier|public
class|class
name|IndicesOptions
block|{
DECL|field|VALUES
specifier|private
specifier|static
specifier|final
name|IndicesOptions
index|[]
name|VALUES
decl_stmt|;
static|static
block|{
name|byte
name|max
init|=
literal|1
operator|<<
literal|4
decl_stmt|;
name|VALUES
operator|=
operator|new
name|IndicesOptions
index|[
name|max
index|]
expr_stmt|;
for|for
control|(
name|byte
name|id
init|=
literal|0
init|;
name|id
operator|<
name|max
condition|;
name|id
operator|++
control|)
block|{
name|VALUES
index|[
name|id
index|]
operator|=
operator|new
name|IndicesOptions
argument_list|(
name|id
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**      * Indices option that ignores unavailable indices, allows no indices and expand wildcards to open only indices      */
DECL|field|IGNORE_UNAVAILABLE_EXPAND_OPEN_ONLY
specifier|public
specifier|static
name|IndicesOptions
name|IGNORE_UNAVAILABLE_EXPAND_OPEN_ONLY
init|=
name|fromOptions
argument_list|(
literal|true
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|)
decl_stmt|;
comment|/**      * Indices option that doesn't ignore unavailable indices, allows no indices and expand wildcards to both open and closed indices      */
DECL|field|ERROR_UNAVAILABLE_EXPAND_OPEN_CLOSE
specifier|public
specifier|static
name|IndicesOptions
name|ERROR_UNAVAILABLE_EXPAND_OPEN_CLOSE
init|=
name|fromOptions
argument_list|(
literal|false
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|)
decl_stmt|;
DECL|field|id
specifier|private
specifier|final
name|byte
name|id
decl_stmt|;
DECL|method|IndicesOptions
specifier|private
name|IndicesOptions
parameter_list|(
name|byte
name|id
parameter_list|)
block|{
name|this
operator|.
name|id
operator|=
name|id
expr_stmt|;
block|}
comment|/**      * @return Whether specified concrete indices should be ignored when unavailable (missing or closed)      */
DECL|method|ignoreUnavailable
specifier|public
name|boolean
name|ignoreUnavailable
parameter_list|()
block|{
return|return
operator|(
name|id
operator|&
literal|1
operator|)
operator|!=
literal|0
return|;
block|}
comment|/**      * @return Whether to ignore if a wildcard indices expression resolves into no concrete indices.      *         The `_all` string or when no indices have been specified also count as wildcard expressions.      */
DECL|method|allowNoIndices
specifier|public
name|boolean
name|allowNoIndices
parameter_list|()
block|{
return|return
operator|(
name|id
operator|&
literal|2
operator|)
operator|!=
literal|0
return|;
block|}
comment|/**      * @return Whether wildcard indices expressions should expanded into open indices should be      */
DECL|method|expandWildcardsOpen
specifier|public
name|boolean
name|expandWildcardsOpen
parameter_list|()
block|{
return|return
operator|(
name|id
operator|&
literal|4
operator|)
operator|!=
literal|0
return|;
block|}
comment|/**      * @return Whether wildcard indices expressions should expanded into closed indices should be      */
DECL|method|expandWildcardsClosed
specifier|public
name|boolean
name|expandWildcardsClosed
parameter_list|()
block|{
return|return
operator|(
name|id
operator|&
literal|8
operator|)
operator|!=
literal|0
return|;
block|}
DECL|method|writeIndicesOptions
specifier|public
name|void
name|writeIndicesOptions
parameter_list|(
name|StreamOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|write
argument_list|(
name|id
argument_list|)
expr_stmt|;
block|}
DECL|method|readIndicesOptions
specifier|public
specifier|static
name|IndicesOptions
name|readIndicesOptions
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|byte
name|id
init|=
name|in
operator|.
name|readByte
argument_list|()
decl_stmt|;
if|if
condition|(
name|id
operator|>=
name|VALUES
operator|.
name|length
condition|)
block|{
throw|throw
operator|new
name|ElasticsearchIllegalArgumentException
argument_list|(
literal|"No valid missing index type id: "
operator|+
name|id
argument_list|)
throw|;
block|}
return|return
name|VALUES
index|[
name|id
index|]
return|;
block|}
DECL|method|fromOptions
specifier|public
specifier|static
name|IndicesOptions
name|fromOptions
parameter_list|(
name|boolean
name|ignoreUnavailable
parameter_list|,
name|boolean
name|allowNoIndices
parameter_list|,
name|boolean
name|expandToOpenIndices
parameter_list|,
name|boolean
name|expandToClosedIndices
parameter_list|)
block|{
name|byte
name|id
init|=
name|toByte
argument_list|(
name|ignoreUnavailable
argument_list|,
name|allowNoIndices
argument_list|,
name|expandToOpenIndices
argument_list|,
name|expandToClosedIndices
argument_list|)
decl_stmt|;
return|return
name|VALUES
index|[
name|id
index|]
return|;
block|}
DECL|method|fromRequest
specifier|public
specifier|static
name|IndicesOptions
name|fromRequest
parameter_list|(
name|RestRequest
name|request
parameter_list|,
name|IndicesOptions
name|defaultSettings
parameter_list|)
block|{
name|String
name|sWildcards
init|=
name|request
operator|.
name|param
argument_list|(
literal|"expand_wildcards"
argument_list|)
decl_stmt|;
name|String
name|sIgnoreUnavailable
init|=
name|request
operator|.
name|param
argument_list|(
literal|"ignore_unavailable"
argument_list|)
decl_stmt|;
name|String
name|sAllowNoIndices
init|=
name|request
operator|.
name|param
argument_list|(
literal|"allow_no_indices"
argument_list|)
decl_stmt|;
if|if
condition|(
name|sWildcards
operator|==
literal|null
operator|&&
name|sIgnoreUnavailable
operator|==
literal|null
operator|&&
name|sAllowNoIndices
operator|==
literal|null
condition|)
block|{
return|return
name|defaultSettings
return|;
block|}
name|boolean
name|expandWildcardsOpen
init|=
name|defaultSettings
operator|.
name|expandWildcardsOpen
argument_list|()
decl_stmt|;
name|boolean
name|expandWildcardsClosed
init|=
name|defaultSettings
operator|.
name|expandWildcardsClosed
argument_list|()
decl_stmt|;
if|if
condition|(
name|sWildcards
operator|!=
literal|null
condition|)
block|{
name|String
index|[]
name|wildcards
init|=
name|Strings
operator|.
name|splitStringByCommaToArray
argument_list|(
name|sWildcards
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|wildcard
range|:
name|wildcards
control|)
block|{
if|if
condition|(
literal|"open"
operator|.
name|equals
argument_list|(
name|wildcard
argument_list|)
condition|)
block|{
name|expandWildcardsOpen
operator|=
literal|true
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"closed"
operator|.
name|equals
argument_list|(
name|wildcard
argument_list|)
condition|)
block|{
name|expandWildcardsClosed
operator|=
literal|true
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|ElasticsearchIllegalArgumentException
argument_list|(
literal|"No valid expand wildcard value ["
operator|+
name|wildcard
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
block|}
return|return
name|fromOptions
argument_list|(
name|toBool
argument_list|(
name|sIgnoreUnavailable
argument_list|,
name|defaultSettings
operator|.
name|ignoreUnavailable
argument_list|()
argument_list|)
argument_list|,
name|toBool
argument_list|(
name|sAllowNoIndices
argument_list|,
name|defaultSettings
operator|.
name|allowNoIndices
argument_list|()
argument_list|)
argument_list|,
name|expandWildcardsOpen
argument_list|,
name|expandWildcardsClosed
argument_list|)
return|;
block|}
comment|/**      * @return indices options that requires any specified index to exists, expands wildcards only to open indices and      *         allow that no indices are resolved from wildcard expressions (not returning an error).      */
DECL|method|strict
specifier|public
specifier|static
name|IndicesOptions
name|strict
parameter_list|()
block|{
return|return
name|VALUES
index|[
literal|6
index|]
return|;
block|}
comment|/**      * @return indices options that ignore unavailable indices, expand wildcards only to open indices and      *         allow that no indices are resolved from wildcard expressions (not returning an error).      */
DECL|method|lenient
specifier|public
specifier|static
name|IndicesOptions
name|lenient
parameter_list|()
block|{
return|return
name|VALUES
index|[
literal|7
index|]
return|;
block|}
DECL|method|toByte
specifier|private
specifier|static
name|byte
name|toByte
parameter_list|(
name|boolean
name|ignoreUnavailable
parameter_list|,
name|boolean
name|allowNoIndices
parameter_list|,
name|boolean
name|wildcardExpandToOpen
parameter_list|,
name|boolean
name|wildcardExpandToClosed
parameter_list|)
block|{
name|byte
name|id
init|=
literal|0
decl_stmt|;
if|if
condition|(
name|ignoreUnavailable
condition|)
block|{
name|id
operator||=
literal|1
expr_stmt|;
block|}
if|if
condition|(
name|allowNoIndices
condition|)
block|{
name|id
operator||=
literal|2
expr_stmt|;
block|}
if|if
condition|(
name|wildcardExpandToOpen
condition|)
block|{
name|id
operator||=
literal|4
expr_stmt|;
block|}
if|if
condition|(
name|wildcardExpandToClosed
condition|)
block|{
name|id
operator||=
literal|8
expr_stmt|;
block|}
return|return
name|id
return|;
block|}
DECL|method|toBool
specifier|private
specifier|static
name|boolean
name|toBool
parameter_list|(
name|String
name|sValue
parameter_list|,
name|boolean
name|defaultValue
parameter_list|)
block|{
if|if
condition|(
name|sValue
operator|==
literal|null
condition|)
block|{
return|return
name|defaultValue
return|;
block|}
return|return
operator|!
operator|(
name|sValue
operator|.
name|equals
argument_list|(
literal|"false"
argument_list|)
operator|||
name|sValue
operator|.
name|equals
argument_list|(
literal|"0"
argument_list|)
operator|||
name|sValue
operator|.
name|equals
argument_list|(
literal|"off"
argument_list|)
operator|)
return|;
block|}
block|}
end_class

end_unit

