begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.get
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|get
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticSearchParseException
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
name|ActionResponse
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
name|Unicode
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
name|collect
operator|.
name|ImmutableMap
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
name|io
operator|.
name|stream
operator|.
name|Streamable
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
name|Iterator
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
name|action
operator|.
name|get
operator|.
name|GetField
operator|.
name|*
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
name|collect
operator|.
name|Iterators
operator|.
name|*
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
name|collect
operator|.
name|Maps
operator|.
name|*
import|;
end_import

begin_comment
comment|/**  * The response of a get action.  *  * @author kimchy (shay.banon)  * @see GetRequest  * @see org.elasticsearch.client.Client#get(GetRequest)  */
end_comment

begin_class
DECL|class|GetResponse
specifier|public
class|class
name|GetResponse
implements|implements
name|ActionResponse
implements|,
name|Streamable
implements|,
name|Iterable
argument_list|<
name|GetField
argument_list|>
block|{
DECL|field|index
specifier|private
name|String
name|index
decl_stmt|;
DECL|field|type
specifier|private
name|String
name|type
decl_stmt|;
DECL|field|id
specifier|private
name|String
name|id
decl_stmt|;
DECL|field|exists
specifier|private
name|boolean
name|exists
decl_stmt|;
DECL|field|fields
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|GetField
argument_list|>
name|fields
decl_stmt|;
DECL|field|sourceAsMap
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|sourceAsMap
decl_stmt|;
DECL|field|source
specifier|private
name|byte
index|[]
name|source
decl_stmt|;
DECL|method|GetResponse
name|GetResponse
parameter_list|()
block|{     }
DECL|method|GetResponse
name|GetResponse
parameter_list|(
name|String
name|index
parameter_list|,
name|String
name|type
parameter_list|,
name|String
name|id
parameter_list|,
name|boolean
name|exists
parameter_list|,
name|byte
index|[]
name|source
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|GetField
argument_list|>
name|fields
parameter_list|)
block|{
name|this
operator|.
name|index
operator|=
name|index
expr_stmt|;
name|this
operator|.
name|type
operator|=
name|type
expr_stmt|;
name|this
operator|.
name|id
operator|=
name|id
expr_stmt|;
name|this
operator|.
name|exists
operator|=
name|exists
expr_stmt|;
name|this
operator|.
name|source
operator|=
name|source
expr_stmt|;
name|this
operator|.
name|fields
operator|=
name|fields
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|fields
operator|==
literal|null
condition|)
block|{
name|this
operator|.
name|fields
operator|=
name|ImmutableMap
operator|.
name|of
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**      * Does the document exists.      */
DECL|method|exists
specifier|public
name|boolean
name|exists
parameter_list|()
block|{
return|return
name|exists
return|;
block|}
comment|/**      * Does the document exists.      */
DECL|method|isExists
specifier|public
name|boolean
name|isExists
parameter_list|()
block|{
return|return
name|exists
return|;
block|}
comment|/**      * The index the document was fetched from.      */
DECL|method|index
specifier|public
name|String
name|index
parameter_list|()
block|{
return|return
name|this
operator|.
name|index
return|;
block|}
comment|/**      * The index the document was fetched from.      */
DECL|method|getIndex
specifier|public
name|String
name|getIndex
parameter_list|()
block|{
return|return
name|index
return|;
block|}
comment|/**      * The type of the document.      */
DECL|method|type
specifier|public
name|String
name|type
parameter_list|()
block|{
return|return
name|type
return|;
block|}
comment|/**      * The type of the document.      */
DECL|method|getType
specifier|public
name|String
name|getType
parameter_list|()
block|{
return|return
name|type
return|;
block|}
comment|/**      * The id of the document.      */
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
comment|/**      * The id of the document.      */
DECL|method|getId
specifier|public
name|String
name|getId
parameter_list|()
block|{
return|return
name|id
return|;
block|}
comment|/**      * The source of the document if exists.      */
DECL|method|source
specifier|public
name|byte
index|[]
name|source
parameter_list|()
block|{
return|return
name|this
operator|.
name|source
return|;
block|}
comment|/**      * The source of the document (as a string).      */
DECL|method|sourceAsString
specifier|public
name|String
name|sourceAsString
parameter_list|()
block|{
if|if
condition|(
name|source
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
name|Unicode
operator|.
name|fromBytes
argument_list|(
name|source
argument_list|)
return|;
block|}
comment|/**      * The source of the document (As a map).      */
annotation|@
name|SuppressWarnings
argument_list|(
block|{
literal|"unchecked"
block|}
argument_list|)
DECL|method|sourceAsMap
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|sourceAsMap
parameter_list|()
throws|throws
name|ElasticSearchParseException
block|{
if|if
condition|(
name|source
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
if|if
condition|(
name|sourceAsMap
operator|!=
literal|null
condition|)
block|{
return|return
name|sourceAsMap
return|;
block|}
name|XContentParser
name|parser
init|=
literal|null
decl_stmt|;
try|try
block|{
name|parser
operator|=
name|XContentFactory
operator|.
name|xContent
argument_list|(
name|source
argument_list|)
operator|.
name|createParser
argument_list|(
name|source
argument_list|)
expr_stmt|;
name|sourceAsMap
operator|=
name|parser
operator|.
name|map
argument_list|()
expr_stmt|;
name|parser
operator|.
name|close
argument_list|()
expr_stmt|;
return|return
name|sourceAsMap
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|ElasticSearchParseException
argument_list|(
literal|"Failed to parse source to map"
argument_list|,
name|e
argument_list|)
throw|;
block|}
finally|finally
block|{
if|if
condition|(
name|parser
operator|!=
literal|null
condition|)
block|{
name|parser
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
DECL|method|getSource
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|getSource
parameter_list|()
block|{
return|return
name|sourceAsMap
argument_list|()
return|;
block|}
DECL|method|fields
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|GetField
argument_list|>
name|fields
parameter_list|()
block|{
return|return
name|this
operator|.
name|fields
return|;
block|}
DECL|method|getFields
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|GetField
argument_list|>
name|getFields
parameter_list|()
block|{
return|return
name|fields
return|;
block|}
DECL|method|field
specifier|public
name|GetField
name|field
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
name|fields
operator|.
name|get
argument_list|(
name|name
argument_list|)
return|;
block|}
DECL|method|iterator
annotation|@
name|Override
specifier|public
name|Iterator
argument_list|<
name|GetField
argument_list|>
name|iterator
parameter_list|()
block|{
if|if
condition|(
name|fields
operator|==
literal|null
condition|)
block|{
return|return
name|emptyIterator
argument_list|()
return|;
block|}
return|return
name|fields
operator|.
name|values
argument_list|()
operator|.
name|iterator
argument_list|()
return|;
block|}
DECL|method|readFrom
annotation|@
name|Override
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
name|index
operator|=
name|in
operator|.
name|readUTF
argument_list|()
expr_stmt|;
name|type
operator|=
name|in
operator|.
name|readUTF
argument_list|()
expr_stmt|;
name|id
operator|=
name|in
operator|.
name|readUTF
argument_list|()
expr_stmt|;
name|exists
operator|=
name|in
operator|.
name|readBoolean
argument_list|()
expr_stmt|;
if|if
condition|(
name|exists
condition|)
block|{
name|int
name|size
init|=
name|in
operator|.
name|readVInt
argument_list|()
decl_stmt|;
if|if
condition|(
name|size
operator|>
literal|0
condition|)
block|{
name|source
operator|=
operator|new
name|byte
index|[
name|size
index|]
expr_stmt|;
name|in
operator|.
name|readFully
argument_list|(
name|source
argument_list|)
expr_stmt|;
block|}
name|size
operator|=
name|in
operator|.
name|readVInt
argument_list|()
expr_stmt|;
if|if
condition|(
name|size
operator|==
literal|0
condition|)
block|{
name|fields
operator|=
name|ImmutableMap
operator|.
name|of
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|fields
operator|=
name|newHashMapWithExpectedSize
argument_list|(
name|size
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|size
condition|;
name|i
operator|++
control|)
block|{
name|GetField
name|field
init|=
name|readGetField
argument_list|(
name|in
argument_list|)
decl_stmt|;
name|fields
operator|.
name|put
argument_list|(
name|field
operator|.
name|name
argument_list|()
argument_list|,
name|field
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
DECL|method|writeTo
annotation|@
name|Override
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
name|out
operator|.
name|writeUTF
argument_list|(
name|index
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeUTF
argument_list|(
name|type
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeUTF
argument_list|(
name|id
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeBoolean
argument_list|(
name|exists
argument_list|)
expr_stmt|;
if|if
condition|(
name|exists
condition|)
block|{
if|if
condition|(
name|source
operator|==
literal|null
condition|)
block|{
name|out
operator|.
name|writeVInt
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|out
operator|.
name|writeVInt
argument_list|(
name|source
operator|.
name|length
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeBytes
argument_list|(
name|source
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|fields
operator|==
literal|null
condition|)
block|{
name|out
operator|.
name|writeVInt
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|out
operator|.
name|writeVInt
argument_list|(
name|fields
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|GetField
name|field
range|:
name|fields
operator|.
name|values
argument_list|()
control|)
block|{
name|field
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
end_class

end_unit

