begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|java
operator|.
name|util
operator|.
name|Objects
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
name|BytesArray
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
name|XContentType
import|;
end_import

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
name|String
name|index
parameter_list|,
name|String
name|type
parameter_list|,
name|String
name|id
parameter_list|,
name|BytesReference
name|source
parameter_list|,
name|XContentType
name|contentType
parameter_list|)
block|{
return|return
name|source
argument_list|(
name|Origin
operator|.
name|PRIMARY
argument_list|,
name|index
argument_list|,
name|type
argument_list|,
name|id
argument_list|,
name|source
argument_list|,
name|contentType
argument_list|)
return|;
block|}
DECL|method|source
specifier|public
specifier|static
name|SourceToParse
name|source
parameter_list|(
name|Origin
name|origin
parameter_list|,
name|String
name|index
parameter_list|,
name|String
name|type
parameter_list|,
name|String
name|id
parameter_list|,
name|BytesReference
name|source
parameter_list|,
name|XContentType
name|contentType
parameter_list|)
block|{
return|return
operator|new
name|SourceToParse
argument_list|(
name|origin
argument_list|,
name|index
argument_list|,
name|type
argument_list|,
name|id
argument_list|,
name|source
argument_list|,
name|contentType
argument_list|)
return|;
block|}
DECL|field|origin
specifier|private
specifier|final
name|Origin
name|origin
decl_stmt|;
DECL|field|source
specifier|private
specifier|final
name|BytesReference
name|source
decl_stmt|;
DECL|field|index
specifier|private
specifier|final
name|String
name|index
decl_stmt|;
DECL|field|type
specifier|private
specifier|final
name|String
name|type
decl_stmt|;
DECL|field|id
specifier|private
specifier|final
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
DECL|field|xContentType
specifier|private
name|XContentType
name|xContentType
decl_stmt|;
DECL|method|SourceToParse
specifier|private
name|SourceToParse
parameter_list|(
name|Origin
name|origin
parameter_list|,
name|String
name|index
parameter_list|,
name|String
name|type
parameter_list|,
name|String
name|id
parameter_list|,
name|BytesReference
name|source
parameter_list|,
name|XContentType
name|xContentType
parameter_list|)
block|{
name|this
operator|.
name|origin
operator|=
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|origin
argument_list|)
expr_stmt|;
name|this
operator|.
name|index
operator|=
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|index
argument_list|)
expr_stmt|;
name|this
operator|.
name|type
operator|=
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|type
argument_list|)
expr_stmt|;
name|this
operator|.
name|id
operator|=
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|id
argument_list|)
expr_stmt|;
comment|// we always convert back to byte array, since we store it and Field only supports bytes..
comment|// so, we might as well do it here, and improve the performance of working with direct byte arrays
name|this
operator|.
name|source
operator|=
operator|new
name|BytesArray
argument_list|(
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|source
argument_list|)
operator|.
name|toBytesRef
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|xContentType
operator|=
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|xContentType
argument_list|)
expr_stmt|;
block|}
DECL|method|origin
specifier|public
name|Origin
name|origin
parameter_list|()
block|{
return|return
name|origin
return|;
block|}
DECL|method|source
specifier|public
name|BytesReference
name|source
parameter_list|()
block|{
return|return
name|this
operator|.
name|source
return|;
block|}
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
DECL|method|getXContentType
specifier|public
name|XContentType
name|getXContentType
parameter_list|()
block|{
return|return
name|this
operator|.
name|xContentType
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
DECL|enum|Origin
specifier|public
enum|enum
name|Origin
block|{
DECL|enum constant|PRIMARY
name|PRIMARY
block|,
DECL|enum constant|REPLICA
name|REPLICA
block|}
block|}
end_class

end_unit

