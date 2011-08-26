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
name|apache
operator|.
name|lucene
operator|.
name|analysis
operator|.
name|Analyzer
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
name|Document
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
import|;
end_import

begin_comment
comment|/**  * The result of parsing a document.  *  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|ParsedDocument
specifier|public
class|class
name|ParsedDocument
block|{
DECL|field|uid
specifier|private
specifier|final
name|String
name|uid
decl_stmt|;
DECL|field|id
specifier|private
specifier|final
name|String
name|id
decl_stmt|;
DECL|field|type
specifier|private
specifier|final
name|String
name|type
decl_stmt|;
DECL|field|routing
specifier|private
specifier|final
name|String
name|routing
decl_stmt|;
DECL|field|timestamp
specifier|private
specifier|final
name|long
name|timestamp
decl_stmt|;
DECL|field|documents
specifier|private
specifier|final
name|List
argument_list|<
name|Document
argument_list|>
name|documents
decl_stmt|;
DECL|field|analyzer
specifier|private
specifier|final
name|Analyzer
name|analyzer
decl_stmt|;
DECL|field|source
specifier|private
specifier|final
name|byte
index|[]
name|source
decl_stmt|;
DECL|field|mappersAdded
specifier|private
name|boolean
name|mappersAdded
decl_stmt|;
DECL|field|parent
specifier|private
name|String
name|parent
decl_stmt|;
DECL|method|ParsedDocument
specifier|public
name|ParsedDocument
parameter_list|(
name|String
name|uid
parameter_list|,
name|String
name|id
parameter_list|,
name|String
name|type
parameter_list|,
name|String
name|routing
parameter_list|,
name|long
name|timestamp
parameter_list|,
name|Document
name|document
parameter_list|,
name|Analyzer
name|analyzer
parameter_list|,
name|byte
index|[]
name|source
parameter_list|,
name|boolean
name|mappersAdded
parameter_list|)
block|{
name|this
argument_list|(
name|uid
argument_list|,
name|id
argument_list|,
name|type
argument_list|,
name|routing
argument_list|,
name|timestamp
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
name|document
argument_list|)
argument_list|,
name|analyzer
argument_list|,
name|source
argument_list|,
name|mappersAdded
argument_list|)
expr_stmt|;
block|}
DECL|method|ParsedDocument
specifier|public
name|ParsedDocument
parameter_list|(
name|String
name|uid
parameter_list|,
name|String
name|id
parameter_list|,
name|String
name|type
parameter_list|,
name|String
name|routing
parameter_list|,
name|long
name|timestamp
parameter_list|,
name|List
argument_list|<
name|Document
argument_list|>
name|documents
parameter_list|,
name|Analyzer
name|analyzer
parameter_list|,
name|byte
index|[]
name|source
parameter_list|,
name|boolean
name|mappersAdded
parameter_list|)
block|{
name|this
operator|.
name|uid
operator|=
name|uid
expr_stmt|;
name|this
operator|.
name|id
operator|=
name|id
expr_stmt|;
name|this
operator|.
name|type
operator|=
name|type
expr_stmt|;
name|this
operator|.
name|routing
operator|=
name|routing
expr_stmt|;
name|this
operator|.
name|timestamp
operator|=
name|timestamp
expr_stmt|;
name|this
operator|.
name|documents
operator|=
name|documents
expr_stmt|;
name|this
operator|.
name|source
operator|=
name|source
expr_stmt|;
name|this
operator|.
name|analyzer
operator|=
name|analyzer
expr_stmt|;
name|this
operator|.
name|mappersAdded
operator|=
name|mappersAdded
expr_stmt|;
block|}
DECL|method|uid
specifier|public
name|String
name|uid
parameter_list|()
block|{
return|return
name|this
operator|.
name|uid
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
DECL|method|rootDoc
specifier|public
name|Document
name|rootDoc
parameter_list|()
block|{
return|return
name|documents
operator|.
name|get
argument_list|(
name|documents
operator|.
name|size
argument_list|()
operator|-
literal|1
argument_list|)
return|;
block|}
DECL|method|docs
specifier|public
name|List
argument_list|<
name|Document
argument_list|>
name|docs
parameter_list|()
block|{
return|return
name|this
operator|.
name|documents
return|;
block|}
DECL|method|analyzer
specifier|public
name|Analyzer
name|analyzer
parameter_list|()
block|{
return|return
name|this
operator|.
name|analyzer
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
DECL|method|parent
specifier|public
name|ParsedDocument
name|parent
parameter_list|(
name|String
name|parent
parameter_list|)
block|{
name|this
operator|.
name|parent
operator|=
name|parent
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
name|parent
return|;
block|}
comment|/**      * Has the parsed document caused for new mappings to be added.      */
DECL|method|mappersAdded
specifier|public
name|boolean
name|mappersAdded
parameter_list|()
block|{
return|return
name|mappersAdded
return|;
block|}
DECL|method|toString
specifier|public
name|String
name|toString
parameter_list|()
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"Document "
argument_list|)
operator|.
name|append
argument_list|(
literal|"uid["
argument_list|)
operator|.
name|append
argument_list|(
name|uid
argument_list|)
operator|.
name|append
argument_list|(
literal|"] doc ["
argument_list|)
operator|.
name|append
argument_list|(
name|documents
argument_list|)
operator|.
name|append
argument_list|(
literal|"]"
argument_list|)
expr_stmt|;
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
end_class

end_unit

