begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.mapper.json
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|mapper
operator|.
name|json
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
name|FieldMapperListener
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
name|IdFieldMapper
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
name|MapperParsingException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|lucene
operator|.
name|Lucene
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
comment|/**  * @author kimchy (Shay Banon)  */
end_comment

begin_class
DECL|class|JsonIdFieldMapper
specifier|public
class|class
name|JsonIdFieldMapper
extends|extends
name|JsonFieldMapper
argument_list|<
name|String
argument_list|>
implements|implements
name|IdFieldMapper
block|{
DECL|class|Defaults
specifier|public
specifier|static
class|class
name|Defaults
extends|extends
name|JsonFieldMapper
operator|.
name|Defaults
block|{
DECL|field|NAME
specifier|public
specifier|static
specifier|final
name|String
name|NAME
init|=
literal|"_id"
decl_stmt|;
DECL|field|INDEX_NAME
specifier|public
specifier|static
specifier|final
name|String
name|INDEX_NAME
init|=
literal|"_id"
decl_stmt|;
DECL|field|INDEX
specifier|public
specifier|static
specifier|final
name|Field
operator|.
name|Index
name|INDEX
init|=
name|Field
operator|.
name|Index
operator|.
name|NOT_ANALYZED
decl_stmt|;
DECL|field|STORE
specifier|public
specifier|static
specifier|final
name|Field
operator|.
name|Store
name|STORE
init|=
name|Field
operator|.
name|Store
operator|.
name|NO
decl_stmt|;
DECL|field|OMIT_NORMS
specifier|public
specifier|static
specifier|final
name|boolean
name|OMIT_NORMS
init|=
literal|true
decl_stmt|;
DECL|field|OMIT_TERM_FREQ_AND_POSITIONS
specifier|public
specifier|static
specifier|final
name|boolean
name|OMIT_TERM_FREQ_AND_POSITIONS
init|=
literal|true
decl_stmt|;
block|}
DECL|class|Builder
specifier|public
specifier|static
class|class
name|Builder
extends|extends
name|JsonFieldMapper
operator|.
name|Builder
argument_list|<
name|Builder
argument_list|,
name|JsonIdFieldMapper
argument_list|>
block|{
DECL|method|Builder
specifier|public
name|Builder
parameter_list|()
block|{
name|super
argument_list|(
name|Defaults
operator|.
name|NAME
argument_list|)
expr_stmt|;
name|indexName
operator|=
name|Defaults
operator|.
name|INDEX_NAME
expr_stmt|;
name|store
operator|=
name|Defaults
operator|.
name|STORE
expr_stmt|;
name|index
operator|=
name|Defaults
operator|.
name|INDEX
expr_stmt|;
name|omitNorms
operator|=
name|Defaults
operator|.
name|OMIT_NORMS
expr_stmt|;
name|omitTermFreqAndPositions
operator|=
name|Defaults
operator|.
name|OMIT_TERM_FREQ_AND_POSITIONS
expr_stmt|;
block|}
DECL|method|build
annotation|@
name|Override
specifier|public
name|JsonIdFieldMapper
name|build
parameter_list|(
name|BuilderContext
name|context
parameter_list|)
block|{
return|return
operator|new
name|JsonIdFieldMapper
argument_list|(
name|name
argument_list|,
name|indexName
argument_list|,
name|store
argument_list|,
name|termVector
argument_list|,
name|boost
argument_list|,
name|omitNorms
argument_list|,
name|omitTermFreqAndPositions
argument_list|)
return|;
block|}
block|}
DECL|method|JsonIdFieldMapper
specifier|protected
name|JsonIdFieldMapper
parameter_list|()
block|{
name|this
argument_list|(
name|Defaults
operator|.
name|NAME
argument_list|,
name|Defaults
operator|.
name|INDEX_NAME
argument_list|)
expr_stmt|;
block|}
DECL|method|JsonIdFieldMapper
specifier|protected
name|JsonIdFieldMapper
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|indexName
parameter_list|)
block|{
name|this
argument_list|(
name|name
argument_list|,
name|indexName
argument_list|,
name|Defaults
operator|.
name|STORE
argument_list|,
name|Defaults
operator|.
name|TERM_VECTOR
argument_list|,
name|Defaults
operator|.
name|BOOST
argument_list|,
name|Defaults
operator|.
name|OMIT_NORMS
argument_list|,
name|Defaults
operator|.
name|OMIT_TERM_FREQ_AND_POSITIONS
argument_list|)
expr_stmt|;
block|}
DECL|method|JsonIdFieldMapper
specifier|public
name|JsonIdFieldMapper
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|indexName
parameter_list|,
name|Field
operator|.
name|Store
name|store
parameter_list|,
name|Field
operator|.
name|TermVector
name|termVector
parameter_list|,
name|float
name|boost
parameter_list|,
name|boolean
name|omitNorms
parameter_list|,
name|boolean
name|omitTermFreqAndPositions
parameter_list|)
block|{
name|super
argument_list|(
name|name
argument_list|,
name|indexName
argument_list|,
name|name
argument_list|,
name|Defaults
operator|.
name|INDEX
argument_list|,
name|store
argument_list|,
name|termVector
argument_list|,
name|boost
argument_list|,
name|omitNorms
argument_list|,
name|omitTermFreqAndPositions
argument_list|,
name|Lucene
operator|.
name|KEYWORD_ANALYZER
argument_list|,
name|Lucene
operator|.
name|KEYWORD_ANALYZER
argument_list|)
expr_stmt|;
block|}
DECL|method|value
annotation|@
name|Override
specifier|public
name|String
name|value
parameter_list|(
name|Document
name|document
parameter_list|)
block|{
name|Fieldable
name|field
init|=
name|document
operator|.
name|getFieldable
argument_list|(
name|indexName
argument_list|)
decl_stmt|;
return|return
name|field
operator|==
literal|null
condition|?
literal|null
else|:
name|value
argument_list|(
name|field
argument_list|)
return|;
block|}
DECL|method|value
annotation|@
name|Override
specifier|public
name|String
name|value
parameter_list|(
name|Fieldable
name|field
parameter_list|)
block|{
return|return
name|field
operator|.
name|stringValue
argument_list|()
return|;
block|}
DECL|method|valueAsString
annotation|@
name|Override
specifier|public
name|String
name|valueAsString
parameter_list|(
name|Fieldable
name|field
parameter_list|)
block|{
return|return
name|value
argument_list|(
name|field
argument_list|)
return|;
block|}
DECL|method|indexedValue
annotation|@
name|Override
specifier|public
name|String
name|indexedValue
parameter_list|(
name|String
name|value
parameter_list|)
block|{
return|return
name|value
return|;
block|}
DECL|method|parseCreateField
annotation|@
name|Override
specifier|protected
name|Field
name|parseCreateField
parameter_list|(
name|JsonParseContext
name|jsonContext
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|jsonContext
operator|.
name|parsedIdState
argument_list|()
operator|==
name|JsonParseContext
operator|.
name|ParsedIdState
operator|.
name|NO
condition|)
block|{
name|String
name|id
init|=
name|jsonContext
operator|.
name|jp
argument_list|()
operator|.
name|getText
argument_list|()
decl_stmt|;
if|if
condition|(
name|jsonContext
operator|.
name|id
argument_list|()
operator|!=
literal|null
operator|&&
operator|!
name|jsonContext
operator|.
name|id
argument_list|()
operator|.
name|equals
argument_list|(
name|id
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|MapperParsingException
argument_list|(
literal|"Provided id ["
operator|+
name|jsonContext
operator|.
name|id
argument_list|()
operator|+
literal|"] does not match the json one ["
operator|+
name|id
operator|+
literal|"]"
argument_list|)
throw|;
block|}
name|jsonContext
operator|.
name|id
argument_list|(
name|id
argument_list|)
expr_stmt|;
name|jsonContext
operator|.
name|parsedId
argument_list|(
name|JsonParseContext
operator|.
name|ParsedIdState
operator|.
name|PARSED
argument_list|)
expr_stmt|;
return|return
operator|new
name|Field
argument_list|(
name|indexName
argument_list|,
name|jsonContext
operator|.
name|id
argument_list|()
argument_list|,
name|store
argument_list|,
name|index
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|jsonContext
operator|.
name|parsedIdState
argument_list|()
operator|==
name|JsonParseContext
operator|.
name|ParsedIdState
operator|.
name|EXTERNAL
condition|)
block|{
if|if
condition|(
name|jsonContext
operator|.
name|id
argument_list|()
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|MapperParsingException
argument_list|(
literal|"No id mapping with ["
operator|+
name|name
argument_list|()
operator|+
literal|"] found in the json, and not explicitly set"
argument_list|)
throw|;
block|}
return|return
operator|new
name|Field
argument_list|(
name|indexName
argument_list|,
name|jsonContext
operator|.
name|id
argument_list|()
argument_list|,
name|store
argument_list|,
name|index
argument_list|)
return|;
block|}
else|else
block|{
throw|throw
operator|new
name|MapperParsingException
argument_list|(
literal|"Illegal parsed id state"
argument_list|)
throw|;
block|}
block|}
DECL|method|traverse
annotation|@
name|Override
specifier|public
name|void
name|traverse
parameter_list|(
name|FieldMapperListener
name|fieldMapperListener
parameter_list|)
block|{
name|fieldMapperListener
operator|.
name|fieldMapper
argument_list|(
name|this
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

