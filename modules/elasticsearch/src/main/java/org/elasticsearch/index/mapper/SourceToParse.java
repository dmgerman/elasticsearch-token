begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.mapper
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|mapper
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
name|xcontent
operator|.
name|XContentParser
import|;
end_import

begin_comment
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|SourceToParse
specifier|public
class|class
name|SourceToParse
block|{
DECL|method|source
specifier|public
specifier|static
name|SourceToParse
name|source
parameter_list|(
name|byte
index|[]
name|source
parameter_list|)
block|{
return|return
operator|new
name|SourceToParse
argument_list|(
name|source
argument_list|)
return|;
block|}
DECL|method|source
specifier|public
specifier|static
name|SourceToParse
name|source
parameter_list|(
name|byte
index|[]
name|source
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|)
block|{
return|return
operator|new
name|SourceToParse
argument_list|(
name|source
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
return|;
block|}
DECL|method|source
specifier|public
specifier|static
name|SourceToParse
name|source
parameter_list|(
name|XContentParser
name|parser
parameter_list|)
block|{
return|return
operator|new
name|SourceToParse
argument_list|(
name|parser
argument_list|)
return|;
block|}
DECL|field|source
specifier|private
specifier|final
name|byte
index|[]
name|source
decl_stmt|;
DECL|field|sourceOffset
specifier|private
specifier|final
name|int
name|sourceOffset
decl_stmt|;
DECL|field|sourceLength
specifier|private
specifier|final
name|int
name|sourceLength
decl_stmt|;
DECL|field|parser
specifier|private
specifier|final
name|XContentParser
name|parser
decl_stmt|;
DECL|field|flyweight
specifier|private
name|boolean
name|flyweight
init|=
literal|false
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
DECL|field|routing
specifier|private
name|String
name|routing
decl_stmt|;
DECL|field|parentId
specifier|private
name|String
name|parentId
decl_stmt|;
DECL|field|timestamp
specifier|private
name|long
name|timestamp
decl_stmt|;
DECL|field|ttl
specifier|private
name|long
name|ttl
decl_stmt|;
DECL|method|SourceToParse
specifier|public
name|SourceToParse
parameter_list|(
name|XContentParser
name|parser
parameter_list|)
block|{
name|this
operator|.
name|parser
operator|=
name|parser
expr_stmt|;
name|this
operator|.
name|source
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|sourceOffset
operator|=
literal|0
expr_stmt|;
name|this
operator|.
name|sourceLength
operator|=
literal|0
expr_stmt|;
block|}
DECL|method|SourceToParse
specifier|public
name|SourceToParse
parameter_list|(
name|byte
index|[]
name|source
parameter_list|)
block|{
name|this
operator|.
name|source
operator|=
name|source
expr_stmt|;
name|this
operator|.
name|sourceOffset
operator|=
literal|0
expr_stmt|;
name|this
operator|.
name|sourceLength
operator|=
name|source
operator|.
name|length
expr_stmt|;
name|this
operator|.
name|parser
operator|=
literal|null
expr_stmt|;
block|}
DECL|method|SourceToParse
specifier|public
name|SourceToParse
parameter_list|(
name|byte
index|[]
name|source
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|)
block|{
name|this
operator|.
name|source
operator|=
name|source
expr_stmt|;
name|this
operator|.
name|sourceOffset
operator|=
name|offset
expr_stmt|;
name|this
operator|.
name|sourceLength
operator|=
name|length
expr_stmt|;
name|this
operator|.
name|parser
operator|=
literal|null
expr_stmt|;
block|}
DECL|method|parser
specifier|public
name|XContentParser
name|parser
parameter_list|()
block|{
return|return
name|this
operator|.
name|parser
return|;
block|}
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
DECL|method|sourceOffset
specifier|public
name|int
name|sourceOffset
parameter_list|()
block|{
return|return
name|this
operator|.
name|sourceOffset
return|;
block|}
DECL|method|sourceLength
specifier|public
name|int
name|sourceLength
parameter_list|()
block|{
return|return
name|this
operator|.
name|sourceLength
return|;
block|}
DECL|method|type
specifier|public
name|String
name|type
parameter_list|()
block|{
return|return
name|this
operator|.
name|type
return|;
block|}
DECL|method|type
specifier|public
name|SourceToParse
name|type
parameter_list|(
name|String
name|type
parameter_list|)
block|{
name|this
operator|.
name|type
operator|=
name|type
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|flyweight
specifier|public
name|SourceToParse
name|flyweight
parameter_list|(
name|boolean
name|flyweight
parameter_list|)
block|{
name|this
operator|.
name|flyweight
operator|=
name|flyweight
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|flyweight
specifier|public
name|boolean
name|flyweight
parameter_list|()
block|{
return|return
name|this
operator|.
name|flyweight
return|;
block|}
DECL|method|id
specifier|public
name|String
name|id
parameter_list|()
block|{
return|return
name|this
operator|.
name|id
return|;
block|}
DECL|method|id
specifier|public
name|SourceToParse
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
DECL|method|parent
specifier|public
name|String
name|parent
parameter_list|()
block|{
return|return
name|this
operator|.
name|parentId
return|;
block|}
DECL|method|parent
specifier|public
name|SourceToParse
name|parent
parameter_list|(
name|String
name|parentId
parameter_list|)
block|{
name|this
operator|.
name|parentId
operator|=
name|parentId
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|routing
specifier|public
name|String
name|routing
parameter_list|()
block|{
return|return
name|this
operator|.
name|routing
return|;
block|}
DECL|method|routing
specifier|public
name|SourceToParse
name|routing
parameter_list|(
name|String
name|routing
parameter_list|)
block|{
name|this
operator|.
name|routing
operator|=
name|routing
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|timestamp
specifier|public
name|long
name|timestamp
parameter_list|()
block|{
return|return
name|this
operator|.
name|timestamp
return|;
block|}
DECL|method|timestamp
specifier|public
name|SourceToParse
name|timestamp
parameter_list|(
name|String
name|timestamp
parameter_list|)
block|{
name|this
operator|.
name|timestamp
operator|=
name|Long
operator|.
name|parseLong
argument_list|(
name|timestamp
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|timestamp
specifier|public
name|SourceToParse
name|timestamp
parameter_list|(
name|long
name|timestamp
parameter_list|)
block|{
name|this
operator|.
name|timestamp
operator|=
name|timestamp
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|ttl
specifier|public
name|long
name|ttl
parameter_list|()
block|{
return|return
name|this
operator|.
name|ttl
return|;
block|}
DECL|method|ttl
specifier|public
name|SourceToParse
name|ttl
parameter_list|(
name|long
name|ttl
parameter_list|)
block|{
name|this
operator|.
name|ttl
operator|=
name|ttl
expr_stmt|;
return|return
name|this
return|;
block|}
block|}
end_class

end_unit

