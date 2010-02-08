begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.util.lucene
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|lucene
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|document
operator|.
name|Document
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|document
operator|.
name|Field
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|document
operator|.
name|Fieldable
import|;
end_import

begin_comment
comment|/**  * @author kimchy (Shay Banon)  */
end_comment

begin_class
DECL|class|DocumentBuilder
specifier|public
class|class
name|DocumentBuilder
block|{
DECL|method|doc
specifier|public
specifier|static
name|DocumentBuilder
name|doc
parameter_list|()
block|{
return|return
operator|new
name|DocumentBuilder
argument_list|()
return|;
block|}
DECL|method|field
specifier|public
specifier|static
name|FieldBuilder
name|field
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|value
parameter_list|)
block|{
return|return
name|field
argument_list|(
name|name
argument_list|,
name|value
argument_list|,
name|Field
operator|.
name|Store
operator|.
name|YES
argument_list|,
name|Field
operator|.
name|Index
operator|.
name|ANALYZED
argument_list|)
return|;
block|}
DECL|method|field
specifier|public
specifier|static
name|FieldBuilder
name|field
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|value
parameter_list|,
name|Field
operator|.
name|Store
name|store
parameter_list|,
name|Field
operator|.
name|Index
name|index
parameter_list|)
block|{
return|return
operator|new
name|FieldBuilder
argument_list|(
name|name
argument_list|,
name|value
argument_list|,
name|store
argument_list|,
name|index
argument_list|)
return|;
block|}
DECL|method|field
specifier|public
specifier|static
name|FieldBuilder
name|field
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|value
parameter_list|,
name|Field
operator|.
name|Store
name|store
parameter_list|,
name|Field
operator|.
name|Index
name|index
parameter_list|,
name|Field
operator|.
name|TermVector
name|termVector
parameter_list|)
block|{
return|return
operator|new
name|FieldBuilder
argument_list|(
name|name
argument_list|,
name|value
argument_list|,
name|store
argument_list|,
name|index
argument_list|,
name|termVector
argument_list|)
return|;
block|}
DECL|method|field
specifier|public
specifier|static
name|FieldBuilder
name|field
parameter_list|(
name|String
name|name
parameter_list|,
name|byte
index|[]
name|value
parameter_list|,
name|Field
operator|.
name|Store
name|store
parameter_list|)
block|{
return|return
operator|new
name|FieldBuilder
argument_list|(
name|name
argument_list|,
name|value
argument_list|,
name|store
argument_list|)
return|;
block|}
DECL|method|field
specifier|public
specifier|static
name|FieldBuilder
name|field
parameter_list|(
name|String
name|name
parameter_list|,
name|byte
index|[]
name|value
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|,
name|Field
operator|.
name|Store
name|store
parameter_list|)
block|{
return|return
operator|new
name|FieldBuilder
argument_list|(
name|name
argument_list|,
name|value
argument_list|,
name|offset
argument_list|,
name|length
argument_list|,
name|store
argument_list|)
return|;
block|}
DECL|field|document
specifier|private
specifier|final
name|Document
name|document
decl_stmt|;
DECL|method|DocumentBuilder
specifier|private
name|DocumentBuilder
parameter_list|()
block|{
name|this
operator|.
name|document
operator|=
operator|new
name|Document
argument_list|()
expr_stmt|;
block|}
DECL|method|boost
specifier|public
name|DocumentBuilder
name|boost
parameter_list|(
name|float
name|boost
parameter_list|)
block|{
name|document
operator|.
name|setBoost
argument_list|(
name|boost
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|add
specifier|public
name|DocumentBuilder
name|add
parameter_list|(
name|Fieldable
name|field
parameter_list|)
block|{
name|document
operator|.
name|add
argument_list|(
name|field
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|add
specifier|public
name|DocumentBuilder
name|add
parameter_list|(
name|FieldBuilder
name|fieldBuilder
parameter_list|)
block|{
name|document
operator|.
name|add
argument_list|(
name|fieldBuilder
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|build
specifier|public
name|Document
name|build
parameter_list|()
block|{
return|return
name|document
return|;
block|}
block|}
end_class

end_unit

