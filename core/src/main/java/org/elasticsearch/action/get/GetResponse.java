begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|ElasticsearchParseException
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
name|get
operator|.
name|GetField
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
name|get
operator|.
name|GetResult
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
import|import
name|java
operator|.
name|util
operator|.
name|Objects
import|;
end_import

begin_comment
comment|/**  * The response of a get action.  *  * @see GetRequest  * @see org.elasticsearch.client.Client#get(GetRequest)  */
end_comment

begin_class
DECL|class|GetResponse
specifier|public
class|class
name|GetResponse
extends|extends
name|ActionResponse
implements|implements
name|Iterable
argument_list|<
name|GetField
argument_list|>
implements|,
name|ToXContentObject
block|{
DECL|field|getResult
name|GetResult
name|getResult
decl_stmt|;
DECL|method|GetResponse
name|GetResponse
parameter_list|()
block|{     }
DECL|method|GetResponse
specifier|public
name|GetResponse
parameter_list|(
name|GetResult
name|getResult
parameter_list|)
block|{
name|this
operator|.
name|getResult
operator|=
name|getResult
expr_stmt|;
block|}
comment|/**      * Does the document exists.      */
DECL|method|isExists
specifier|public
name|boolean
name|isExists
parameter_list|()
block|{
return|return
name|getResult
operator|.
name|isExists
argument_list|()
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
name|getResult
operator|.
name|getIndex
argument_list|()
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
name|getResult
operator|.
name|getType
argument_list|()
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
name|getResult
operator|.
name|getId
argument_list|()
return|;
block|}
comment|/**      * The version of the doc.      */
DECL|method|getVersion
specifier|public
name|long
name|getVersion
parameter_list|()
block|{
return|return
name|getResult
operator|.
name|getVersion
argument_list|()
return|;
block|}
comment|/**      * The source of the document if exists.      */
DECL|method|getSourceAsBytes
specifier|public
name|byte
index|[]
name|getSourceAsBytes
parameter_list|()
block|{
return|return
name|getResult
operator|.
name|source
argument_list|()
return|;
block|}
comment|/**      * Returns the internal source bytes, as they are returned without munging (for example,      * might still be compressed).      */
DECL|method|getSourceInternal
specifier|public
name|BytesReference
name|getSourceInternal
parameter_list|()
block|{
return|return
name|getResult
operator|.
name|internalSourceRef
argument_list|()
return|;
block|}
comment|/**      * Returns bytes reference, also un compress the source if needed.      */
DECL|method|getSourceAsBytesRef
specifier|public
name|BytesReference
name|getSourceAsBytesRef
parameter_list|()
block|{
return|return
name|getResult
operator|.
name|sourceRef
argument_list|()
return|;
block|}
comment|/**      * Is the source empty (not available) or not.      */
DECL|method|isSourceEmpty
specifier|public
name|boolean
name|isSourceEmpty
parameter_list|()
block|{
return|return
name|getResult
operator|.
name|isSourceEmpty
argument_list|()
return|;
block|}
comment|/**      * The source of the document (as a string).      */
DECL|method|getSourceAsString
specifier|public
name|String
name|getSourceAsString
parameter_list|()
block|{
return|return
name|getResult
operator|.
name|sourceAsString
argument_list|()
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
DECL|method|getSourceAsMap
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|getSourceAsMap
parameter_list|()
throws|throws
name|ElasticsearchParseException
block|{
return|return
name|getResult
operator|.
name|sourceAsMap
argument_list|()
return|;
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
name|getResult
operator|.
name|getSource
argument_list|()
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
name|getResult
operator|.
name|getFields
argument_list|()
return|;
block|}
DECL|method|getField
specifier|public
name|GetField
name|getField
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
name|getResult
operator|.
name|field
argument_list|(
name|name
argument_list|)
return|;
block|}
comment|/**      * @deprecated Use {@link GetResponse#getSource()} instead      */
annotation|@
name|Deprecated
annotation|@
name|Override
DECL|method|iterator
specifier|public
name|Iterator
argument_list|<
name|GetField
argument_list|>
name|iterator
parameter_list|()
block|{
return|return
name|getResult
operator|.
name|iterator
argument_list|()
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
return|return
name|getResult
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|params
argument_list|)
return|;
block|}
DECL|method|fromXContent
specifier|public
specifier|static
name|GetResponse
name|fromXContent
parameter_list|(
name|XContentParser
name|parser
parameter_list|)
throws|throws
name|IOException
block|{
name|GetResult
name|getResult
init|=
name|GetResult
operator|.
name|fromXContent
argument_list|(
name|parser
argument_list|)
decl_stmt|;
return|return
operator|new
name|GetResponse
argument_list|(
name|getResult
argument_list|)
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
name|getResult
operator|=
name|GetResult
operator|.
name|readGetResult
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
name|getResult
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|equals
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
if|if
condition|(
name|this
operator|==
name|o
condition|)
block|{
return|return
literal|true
return|;
block|}
if|if
condition|(
name|o
operator|==
literal|null
operator|||
name|getClass
argument_list|()
operator|!=
name|o
operator|.
name|getClass
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
name|GetResponse
name|getResponse
init|=
operator|(
name|GetResponse
operator|)
name|o
decl_stmt|;
return|return
name|Objects
operator|.
name|equals
argument_list|(
name|getResult
argument_list|,
name|getResponse
operator|.
name|getResult
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|hashCode
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
name|Objects
operator|.
name|hash
argument_list|(
name|getResult
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|toString
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|Strings
operator|.
name|toString
argument_list|(
name|this
argument_list|)
return|;
block|}
block|}
end_class

end_unit

