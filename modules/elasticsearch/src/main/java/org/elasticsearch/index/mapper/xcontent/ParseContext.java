begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.mapper.xcontent
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|mapper
operator|.
name|xcontent
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
name|elasticsearch
operator|.
name|common
operator|.
name|lucene
operator|.
name|all
operator|.
name|AllEntries
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
name|util
operator|.
name|concurrent
operator|.
name|NotThreadSafe
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
name|mapper
operator|.
name|DocumentMapper
import|;
end_import

begin_comment
comment|/**  * @author kimchy (Shay Banon)  */
end_comment

begin_class
annotation|@
name|NotThreadSafe
DECL|class|ParseContext
specifier|public
class|class
name|ParseContext
block|{
DECL|field|docMapper
specifier|private
specifier|final
name|XContentDocumentMapper
name|docMapper
decl_stmt|;
DECL|field|docMapperParser
specifier|private
specifier|final
name|XContentDocumentMapperParser
name|docMapperParser
decl_stmt|;
DECL|field|path
specifier|private
specifier|final
name|ContentPath
name|path
decl_stmt|;
DECL|field|parser
specifier|private
name|XContentParser
name|parser
decl_stmt|;
DECL|field|document
specifier|private
name|Document
name|document
decl_stmt|;
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
DECL|field|source
specifier|private
name|byte
index|[]
name|source
decl_stmt|;
DECL|field|id
specifier|private
name|String
name|id
decl_stmt|;
DECL|field|listener
specifier|private
name|DocumentMapper
operator|.
name|ParseListener
name|listener
decl_stmt|;
DECL|field|uid
specifier|private
name|String
name|uid
decl_stmt|;
DECL|field|stringBuilder
specifier|private
name|StringBuilder
name|stringBuilder
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
DECL|field|parsedIdState
specifier|private
name|ParsedIdState
name|parsedIdState
decl_stmt|;
DECL|field|mappersAdded
specifier|private
name|boolean
name|mappersAdded
init|=
literal|false
decl_stmt|;
DECL|field|externalValueSet
specifier|private
name|boolean
name|externalValueSet
decl_stmt|;
DECL|field|externalValue
specifier|private
name|Object
name|externalValue
decl_stmt|;
DECL|field|allEntries
specifier|private
name|AllEntries
name|allEntries
init|=
operator|new
name|AllEntries
argument_list|()
decl_stmt|;
DECL|method|ParseContext
specifier|public
name|ParseContext
parameter_list|(
name|String
name|index
parameter_list|,
name|XContentDocumentMapperParser
name|docMapperParser
parameter_list|,
name|XContentDocumentMapper
name|docMapper
parameter_list|,
name|ContentPath
name|path
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
name|docMapper
operator|=
name|docMapper
expr_stmt|;
name|this
operator|.
name|docMapperParser
operator|=
name|docMapperParser
expr_stmt|;
name|this
operator|.
name|path
operator|=
name|path
expr_stmt|;
block|}
DECL|method|reset
specifier|public
name|void
name|reset
parameter_list|(
name|XContentParser
name|parser
parameter_list|,
name|Document
name|document
parameter_list|,
name|String
name|type
parameter_list|,
name|byte
index|[]
name|source
parameter_list|,
name|DocumentMapper
operator|.
name|ParseListener
name|listener
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
name|document
operator|=
name|document
expr_stmt|;
name|this
operator|.
name|type
operator|=
name|type
expr_stmt|;
name|this
operator|.
name|source
operator|=
name|source
expr_stmt|;
name|this
operator|.
name|path
operator|.
name|reset
argument_list|()
expr_stmt|;
name|this
operator|.
name|parsedIdState
operator|=
name|ParsedIdState
operator|.
name|NO
expr_stmt|;
name|this
operator|.
name|mappersAdded
operator|=
literal|false
expr_stmt|;
name|this
operator|.
name|listener
operator|=
name|listener
expr_stmt|;
name|this
operator|.
name|allEntries
operator|=
operator|new
name|AllEntries
argument_list|()
expr_stmt|;
block|}
DECL|method|docMapperParser
specifier|public
name|XContentDocumentMapperParser
name|docMapperParser
parameter_list|()
block|{
return|return
name|this
operator|.
name|docMapperParser
return|;
block|}
DECL|method|mappersAdded
specifier|public
name|boolean
name|mappersAdded
parameter_list|()
block|{
return|return
name|this
operator|.
name|mappersAdded
return|;
block|}
DECL|method|addedMapper
specifier|public
name|void
name|addedMapper
parameter_list|()
block|{
name|this
operator|.
name|mappersAdded
operator|=
literal|true
expr_stmt|;
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
DECL|method|path
specifier|public
name|ContentPath
name|path
parameter_list|()
block|{
return|return
name|this
operator|.
name|path
return|;
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
DECL|method|listener
specifier|public
name|DocumentMapper
operator|.
name|ParseListener
name|listener
parameter_list|()
block|{
return|return
name|this
operator|.
name|listener
return|;
block|}
DECL|method|doc
specifier|public
name|Document
name|doc
parameter_list|()
block|{
return|return
name|this
operator|.
name|document
return|;
block|}
DECL|method|root
specifier|public
name|RootObjectMapper
name|root
parameter_list|()
block|{
return|return
name|docMapper
operator|.
name|root
argument_list|()
return|;
block|}
DECL|method|docMapper
specifier|public
name|XContentDocumentMapper
name|docMapper
parameter_list|()
block|{
return|return
name|this
operator|.
name|docMapper
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
DECL|method|parsedId
specifier|public
name|void
name|parsedId
parameter_list|(
name|ParsedIdState
name|parsedIdState
parameter_list|)
block|{
name|this
operator|.
name|parsedIdState
operator|=
name|parsedIdState
expr_stmt|;
block|}
DECL|method|parsedIdState
specifier|public
name|ParsedIdState
name|parsedIdState
parameter_list|()
block|{
return|return
name|this
operator|.
name|parsedIdState
return|;
block|}
comment|/**      * Really, just the id mapper should set this.      */
DECL|method|id
specifier|public
name|void
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
comment|/**      * Really, just the uid mapper should set this.      */
DECL|method|uid
specifier|public
name|void
name|uid
parameter_list|(
name|String
name|uid
parameter_list|)
block|{
name|this
operator|.
name|uid
operator|=
name|uid
expr_stmt|;
block|}
DECL|method|allEntries
specifier|public
name|AllEntries
name|allEntries
parameter_list|()
block|{
return|return
name|this
operator|.
name|allEntries
return|;
block|}
DECL|method|externalValue
specifier|public
name|void
name|externalValue
parameter_list|(
name|Object
name|externalValue
parameter_list|)
block|{
name|this
operator|.
name|externalValueSet
operator|=
literal|true
expr_stmt|;
name|this
operator|.
name|externalValue
operator|=
name|externalValue
expr_stmt|;
block|}
DECL|method|externalValueSet
specifier|public
name|boolean
name|externalValueSet
parameter_list|()
block|{
return|return
name|this
operator|.
name|externalValueSet
return|;
block|}
DECL|method|externalValue
specifier|public
name|Object
name|externalValue
parameter_list|()
block|{
name|externalValueSet
operator|=
literal|false
expr_stmt|;
return|return
name|externalValue
return|;
block|}
comment|/**      * A string builder that can be used to construct complex names for example.      * Its better to reuse the.      */
DECL|method|stringBuilder
specifier|public
name|StringBuilder
name|stringBuilder
parameter_list|()
block|{
name|stringBuilder
operator|.
name|setLength
argument_list|(
literal|0
argument_list|)
expr_stmt|;
return|return
name|this
operator|.
name|stringBuilder
return|;
block|}
DECL|enum|ParsedIdState
specifier|public
specifier|static
enum|enum
name|ParsedIdState
block|{
DECL|enum constant|NO
name|NO
block|,
DECL|enum constant|PARSED
name|PARSED
block|,
DECL|enum constant|EXTERNAL
name|EXTERNAL
block|}
block|}
end_class

end_unit

