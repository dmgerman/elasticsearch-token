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
name|org
operator|.
name|codehaus
operator|.
name|jackson
operator|.
name|JsonFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|codehaus
operator|.
name|jackson
operator|.
name|JsonParser
import|;
end_import

begin_import
import|import
name|org
operator|.
name|codehaus
operator|.
name|jackson
operator|.
name|JsonToken
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
name|analysis
operator|.
name|NamedAnalyzer
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
name|*
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
name|Nullable
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
name|Preconditions
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
name|ThreadLocals
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
name|json
operator|.
name|Jackson
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
name|json
operator|.
name|JsonBuilder
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
name|json
operator|.
name|StringJsonBuilder
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
name|json
operator|.
name|ToJson
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
name|List
import|;
end_import

begin_import
import|import static
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Lists
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
name|util
operator|.
name|json
operator|.
name|JsonBuilder
operator|.
name|*
import|;
end_import

begin_comment
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_class
specifier|public
class|class
DECL|class|JsonDocumentMapper
name|JsonDocumentMapper
implements|implements
name|DocumentMapper
implements|,
name|ToJson
block|{
DECL|class|Builder
specifier|public
specifier|static
class|class
name|Builder
block|{
DECL|field|uidFieldMapper
specifier|private
name|JsonUidFieldMapper
name|uidFieldMapper
init|=
operator|new
name|JsonUidFieldMapper
argument_list|()
decl_stmt|;
DECL|field|idFieldMapper
specifier|private
name|JsonIdFieldMapper
name|idFieldMapper
init|=
operator|new
name|JsonIdFieldMapper
argument_list|()
decl_stmt|;
DECL|field|typeFieldMapper
specifier|private
name|JsonTypeFieldMapper
name|typeFieldMapper
init|=
operator|new
name|JsonTypeFieldMapper
argument_list|()
decl_stmt|;
DECL|field|sourceFieldMapper
specifier|private
name|JsonSourceFieldMapper
name|sourceFieldMapper
init|=
operator|new
name|JsonSourceFieldMapper
argument_list|()
decl_stmt|;
DECL|field|boostFieldMapper
specifier|private
name|JsonBoostFieldMapper
name|boostFieldMapper
init|=
operator|new
name|JsonBoostFieldMapper
argument_list|()
decl_stmt|;
DECL|field|allFieldMapper
specifier|private
name|JsonAllFieldMapper
name|allFieldMapper
init|=
operator|new
name|JsonAllFieldMapper
argument_list|()
decl_stmt|;
DECL|field|indexAnalyzer
specifier|private
name|NamedAnalyzer
name|indexAnalyzer
decl_stmt|;
DECL|field|searchAnalyzer
specifier|private
name|NamedAnalyzer
name|searchAnalyzer
decl_stmt|;
DECL|field|rootObjectMapper
specifier|private
specifier|final
name|JsonObjectMapper
name|rootObjectMapper
decl_stmt|;
DECL|field|mappingSource
specifier|private
name|String
name|mappingSource
decl_stmt|;
DECL|field|builderContext
specifier|private
name|JsonMapper
operator|.
name|BuilderContext
name|builderContext
init|=
operator|new
name|JsonMapper
operator|.
name|BuilderContext
argument_list|(
operator|new
name|JsonPath
argument_list|(
literal|1
argument_list|)
argument_list|)
decl_stmt|;
DECL|method|Builder
specifier|public
name|Builder
parameter_list|(
name|JsonObjectMapper
operator|.
name|Builder
name|builder
parameter_list|)
block|{
name|this
operator|.
name|rootObjectMapper
operator|=
name|builder
operator|.
name|build
argument_list|(
name|builderContext
argument_list|)
expr_stmt|;
block|}
DECL|method|sourceField
specifier|public
name|Builder
name|sourceField
parameter_list|(
name|JsonSourceFieldMapper
operator|.
name|Builder
name|builder
parameter_list|)
block|{
name|this
operator|.
name|sourceFieldMapper
operator|=
name|builder
operator|.
name|build
argument_list|(
name|builderContext
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|idField
specifier|public
name|Builder
name|idField
parameter_list|(
name|JsonIdFieldMapper
operator|.
name|Builder
name|builder
parameter_list|)
block|{
name|this
operator|.
name|idFieldMapper
operator|=
name|builder
operator|.
name|build
argument_list|(
name|builderContext
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|uidField
specifier|public
name|Builder
name|uidField
parameter_list|(
name|JsonUidFieldMapper
operator|.
name|Builder
name|builder
parameter_list|)
block|{
name|this
operator|.
name|uidFieldMapper
operator|=
name|builder
operator|.
name|build
argument_list|(
name|builderContext
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|typeField
specifier|public
name|Builder
name|typeField
parameter_list|(
name|JsonTypeFieldMapper
operator|.
name|Builder
name|builder
parameter_list|)
block|{
name|this
operator|.
name|typeFieldMapper
operator|=
name|builder
operator|.
name|build
argument_list|(
name|builderContext
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|boostField
specifier|public
name|Builder
name|boostField
parameter_list|(
name|JsonBoostFieldMapper
operator|.
name|Builder
name|builder
parameter_list|)
block|{
name|this
operator|.
name|boostFieldMapper
operator|=
name|builder
operator|.
name|build
argument_list|(
name|builderContext
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|allField
specifier|public
name|Builder
name|allField
parameter_list|(
name|JsonAllFieldMapper
operator|.
name|Builder
name|builder
parameter_list|)
block|{
name|this
operator|.
name|allFieldMapper
operator|=
name|builder
operator|.
name|build
argument_list|(
name|builderContext
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|mappingSource
specifier|public
name|Builder
name|mappingSource
parameter_list|(
name|String
name|mappingSource
parameter_list|)
block|{
name|this
operator|.
name|mappingSource
operator|=
name|mappingSource
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|indexAnalyzer
specifier|public
name|Builder
name|indexAnalyzer
parameter_list|(
name|NamedAnalyzer
name|indexAnalyzer
parameter_list|)
block|{
name|this
operator|.
name|indexAnalyzer
operator|=
name|indexAnalyzer
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|hasIndexAnalyzer
specifier|public
name|boolean
name|hasIndexAnalyzer
parameter_list|()
block|{
return|return
name|indexAnalyzer
operator|!=
literal|null
return|;
block|}
DECL|method|searchAnalyzer
specifier|public
name|Builder
name|searchAnalyzer
parameter_list|(
name|NamedAnalyzer
name|searchAnalyzer
parameter_list|)
block|{
name|this
operator|.
name|searchAnalyzer
operator|=
name|searchAnalyzer
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|hasSearchAnalyzer
specifier|public
name|boolean
name|hasSearchAnalyzer
parameter_list|()
block|{
return|return
name|searchAnalyzer
operator|!=
literal|null
return|;
block|}
DECL|method|build
specifier|public
name|JsonDocumentMapper
name|build
parameter_list|()
block|{
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|rootObjectMapper
argument_list|,
literal|"Json mapper builder must have the root object mapper set"
argument_list|)
expr_stmt|;
return|return
operator|new
name|JsonDocumentMapper
argument_list|(
name|rootObjectMapper
argument_list|,
name|uidFieldMapper
argument_list|,
name|idFieldMapper
argument_list|,
name|typeFieldMapper
argument_list|,
name|sourceFieldMapper
argument_list|,
name|allFieldMapper
argument_list|,
name|indexAnalyzer
argument_list|,
name|searchAnalyzer
argument_list|,
name|boostFieldMapper
argument_list|,
name|mappingSource
argument_list|)
return|;
block|}
block|}
DECL|field|cache
specifier|private
name|ThreadLocal
argument_list|<
name|ThreadLocals
operator|.
name|CleanableValue
argument_list|<
name|JsonParseContext
argument_list|>
argument_list|>
name|cache
init|=
operator|new
name|ThreadLocal
argument_list|<
name|ThreadLocals
operator|.
name|CleanableValue
argument_list|<
name|JsonParseContext
argument_list|>
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|protected
name|ThreadLocals
operator|.
name|CleanableValue
argument_list|<
name|JsonParseContext
argument_list|>
name|initialValue
parameter_list|()
block|{
return|return
operator|new
name|ThreadLocals
operator|.
name|CleanableValue
argument_list|<
name|JsonParseContext
argument_list|>
argument_list|(
operator|new
name|JsonParseContext
argument_list|(
name|JsonDocumentMapper
operator|.
name|this
argument_list|,
operator|new
name|JsonPath
argument_list|(
literal|0
argument_list|)
argument_list|)
argument_list|)
return|;
block|}
block|}
decl_stmt|;
DECL|field|jsonFactory
specifier|private
specifier|final
name|JsonFactory
name|jsonFactory
init|=
name|Jackson
operator|.
name|defaultJsonFactory
argument_list|()
decl_stmt|;
DECL|field|type
specifier|private
specifier|final
name|String
name|type
decl_stmt|;
DECL|field|mappingSource
specifier|private
specifier|volatile
name|String
name|mappingSource
decl_stmt|;
DECL|field|uidFieldMapper
specifier|private
specifier|final
name|JsonUidFieldMapper
name|uidFieldMapper
decl_stmt|;
DECL|field|idFieldMapper
specifier|private
specifier|final
name|JsonIdFieldMapper
name|idFieldMapper
decl_stmt|;
DECL|field|typeFieldMapper
specifier|private
specifier|final
name|JsonTypeFieldMapper
name|typeFieldMapper
decl_stmt|;
DECL|field|sourceFieldMapper
specifier|private
specifier|final
name|JsonSourceFieldMapper
name|sourceFieldMapper
decl_stmt|;
DECL|field|boostFieldMapper
specifier|private
specifier|final
name|JsonBoostFieldMapper
name|boostFieldMapper
decl_stmt|;
DECL|field|allFieldMapper
specifier|private
specifier|final
name|JsonAllFieldMapper
name|allFieldMapper
decl_stmt|;
DECL|field|rootObjectMapper
specifier|private
specifier|final
name|JsonObjectMapper
name|rootObjectMapper
decl_stmt|;
DECL|field|indexAnalyzer
specifier|private
specifier|final
name|Analyzer
name|indexAnalyzer
decl_stmt|;
DECL|field|searchAnalyzer
specifier|private
specifier|final
name|Analyzer
name|searchAnalyzer
decl_stmt|;
DECL|field|fieldMappers
specifier|private
specifier|volatile
name|DocumentFieldMappers
name|fieldMappers
decl_stmt|;
DECL|field|fieldMapperListeners
specifier|private
specifier|final
name|List
argument_list|<
name|FieldMapperListener
argument_list|>
name|fieldMapperListeners
init|=
name|newArrayList
argument_list|()
decl_stmt|;
DECL|field|mutex
specifier|private
specifier|final
name|Object
name|mutex
init|=
operator|new
name|Object
argument_list|()
decl_stmt|;
DECL|method|JsonDocumentMapper
specifier|public
name|JsonDocumentMapper
parameter_list|(
name|JsonObjectMapper
name|rootObjectMapper
parameter_list|,
name|JsonUidFieldMapper
name|uidFieldMapper
parameter_list|,
name|JsonIdFieldMapper
name|idFieldMapper
parameter_list|,
name|JsonTypeFieldMapper
name|typeFieldMapper
parameter_list|,
name|JsonSourceFieldMapper
name|sourceFieldMapper
parameter_list|,
name|JsonAllFieldMapper
name|allFieldMapper
parameter_list|,
name|Analyzer
name|indexAnalyzer
parameter_list|,
name|Analyzer
name|searchAnalyzer
parameter_list|,
annotation|@
name|Nullable
name|JsonBoostFieldMapper
name|boostFieldMapper
parameter_list|,
annotation|@
name|Nullable
name|String
name|mappingSource
parameter_list|)
block|{
name|this
operator|.
name|type
operator|=
name|rootObjectMapper
operator|.
name|name
argument_list|()
expr_stmt|;
name|this
operator|.
name|mappingSource
operator|=
name|mappingSource
expr_stmt|;
name|this
operator|.
name|rootObjectMapper
operator|=
name|rootObjectMapper
expr_stmt|;
name|this
operator|.
name|uidFieldMapper
operator|=
name|uidFieldMapper
expr_stmt|;
name|this
operator|.
name|idFieldMapper
operator|=
name|idFieldMapper
expr_stmt|;
name|this
operator|.
name|typeFieldMapper
operator|=
name|typeFieldMapper
expr_stmt|;
name|this
operator|.
name|sourceFieldMapper
operator|=
name|sourceFieldMapper
expr_stmt|;
name|this
operator|.
name|allFieldMapper
operator|=
name|allFieldMapper
expr_stmt|;
name|this
operator|.
name|boostFieldMapper
operator|=
name|boostFieldMapper
expr_stmt|;
name|this
operator|.
name|indexAnalyzer
operator|=
name|indexAnalyzer
expr_stmt|;
name|this
operator|.
name|searchAnalyzer
operator|=
name|searchAnalyzer
expr_stmt|;
comment|// if we are not enabling all, set it to false on the root object, (and on all the rest...)
if|if
condition|(
operator|!
name|allFieldMapper
operator|.
name|enabled
argument_list|()
condition|)
block|{
name|this
operator|.
name|rootObjectMapper
operator|.
name|includeInAll
argument_list|(
name|allFieldMapper
operator|.
name|enabled
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|rootObjectMapper
operator|.
name|putMapper
argument_list|(
name|idFieldMapper
argument_list|)
expr_stmt|;
if|if
condition|(
name|boostFieldMapper
operator|!=
literal|null
condition|)
block|{
name|rootObjectMapper
operator|.
name|putMapper
argument_list|(
name|boostFieldMapper
argument_list|)
expr_stmt|;
block|}
specifier|final
name|List
argument_list|<
name|FieldMapper
argument_list|>
name|tempFieldMappers
init|=
name|newArrayList
argument_list|()
decl_stmt|;
comment|// add the basic ones
name|tempFieldMappers
operator|.
name|add
argument_list|(
name|typeFieldMapper
argument_list|)
expr_stmt|;
name|tempFieldMappers
operator|.
name|add
argument_list|(
name|sourceFieldMapper
argument_list|)
expr_stmt|;
name|tempFieldMappers
operator|.
name|add
argument_list|(
name|uidFieldMapper
argument_list|)
expr_stmt|;
name|tempFieldMappers
operator|.
name|add
argument_list|(
name|allFieldMapper
argument_list|)
expr_stmt|;
if|if
condition|(
name|boostFieldMapper
operator|!=
literal|null
condition|)
block|{
name|tempFieldMappers
operator|.
name|add
argument_list|(
name|boostFieldMapper
argument_list|)
expr_stmt|;
block|}
comment|// now traverse and get all the statically defined ones
name|rootObjectMapper
operator|.
name|traverse
argument_list|(
operator|new
name|FieldMapperListener
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|fieldMapper
parameter_list|(
name|FieldMapper
name|fieldMapper
parameter_list|)
block|{
name|tempFieldMappers
operator|.
name|add
argument_list|(
name|fieldMapper
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
name|this
operator|.
name|fieldMappers
operator|=
operator|new
name|DocumentFieldMappers
argument_list|(
name|this
argument_list|,
name|tempFieldMappers
argument_list|)
expr_stmt|;
block|}
DECL|method|type
annotation|@
name|Override
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
DECL|method|mappingSource
annotation|@
name|Override
specifier|public
name|String
name|mappingSource
parameter_list|()
block|{
return|return
name|this
operator|.
name|mappingSource
return|;
block|}
DECL|method|mappingSource
name|void
name|mappingSource
parameter_list|(
name|String
name|mappingSource
parameter_list|)
block|{
name|this
operator|.
name|mappingSource
operator|=
name|mappingSource
expr_stmt|;
block|}
DECL|method|uidMapper
annotation|@
name|Override
specifier|public
name|UidFieldMapper
name|uidMapper
parameter_list|()
block|{
return|return
name|this
operator|.
name|uidFieldMapper
return|;
block|}
DECL|method|idMapper
annotation|@
name|Override
specifier|public
name|IdFieldMapper
name|idMapper
parameter_list|()
block|{
return|return
name|this
operator|.
name|idFieldMapper
return|;
block|}
DECL|method|typeMapper
annotation|@
name|Override
specifier|public
name|TypeFieldMapper
name|typeMapper
parameter_list|()
block|{
return|return
name|this
operator|.
name|typeFieldMapper
return|;
block|}
DECL|method|sourceMapper
annotation|@
name|Override
specifier|public
name|SourceFieldMapper
name|sourceMapper
parameter_list|()
block|{
return|return
name|this
operator|.
name|sourceFieldMapper
return|;
block|}
DECL|method|boostMapper
annotation|@
name|Override
specifier|public
name|BoostFieldMapper
name|boostMapper
parameter_list|()
block|{
return|return
name|this
operator|.
name|boostFieldMapper
return|;
block|}
DECL|method|allFieldMapper
annotation|@
name|Override
specifier|public
name|AllFieldMapper
name|allFieldMapper
parameter_list|()
block|{
return|return
name|this
operator|.
name|allFieldMapper
return|;
block|}
DECL|method|indexAnalyzer
annotation|@
name|Override
specifier|public
name|Analyzer
name|indexAnalyzer
parameter_list|()
block|{
return|return
name|this
operator|.
name|indexAnalyzer
return|;
block|}
DECL|method|searchAnalyzer
annotation|@
name|Override
specifier|public
name|Analyzer
name|searchAnalyzer
parameter_list|()
block|{
return|return
name|this
operator|.
name|searchAnalyzer
return|;
block|}
DECL|method|mappers
annotation|@
name|Override
specifier|public
name|DocumentFieldMappers
name|mappers
parameter_list|()
block|{
return|return
name|this
operator|.
name|fieldMappers
return|;
block|}
DECL|method|parse
annotation|@
name|Override
specifier|public
name|ParsedDocument
name|parse
parameter_list|(
name|byte
index|[]
name|source
parameter_list|)
block|{
return|return
name|parse
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|,
name|source
argument_list|)
return|;
block|}
DECL|method|parse
annotation|@
name|Override
specifier|public
name|ParsedDocument
name|parse
parameter_list|(
annotation|@
name|Nullable
name|String
name|type
parameter_list|,
annotation|@
name|Nullable
name|String
name|id
parameter_list|,
name|byte
index|[]
name|source
parameter_list|)
throws|throws
name|MapperParsingException
block|{
return|return
name|parse
argument_list|(
name|type
argument_list|,
name|id
argument_list|,
name|source
argument_list|,
name|ParseListener
operator|.
name|EMPTY
argument_list|)
return|;
block|}
DECL|method|parse
annotation|@
name|Override
specifier|public
name|ParsedDocument
name|parse
parameter_list|(
name|String
name|type
parameter_list|,
name|String
name|id
parameter_list|,
name|byte
index|[]
name|source
parameter_list|,
name|ParseListener
name|listener
parameter_list|)
block|{
name|JsonParseContext
name|jsonContext
init|=
name|cache
operator|.
name|get
argument_list|()
operator|.
name|get
argument_list|()
decl_stmt|;
if|if
condition|(
name|type
operator|!=
literal|null
operator|&&
operator|!
name|type
operator|.
name|equals
argument_list|(
name|this
operator|.
name|type
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|MapperParsingException
argument_list|(
literal|"Type mismatch, provide type ["
operator|+
name|type
operator|+
literal|"] but mapper is of type ["
operator|+
name|this
operator|.
name|type
operator|+
literal|"]"
argument_list|)
throw|;
block|}
name|type
operator|=
name|this
operator|.
name|type
expr_stmt|;
name|JsonParser
name|jp
init|=
literal|null
decl_stmt|;
try|try
block|{
name|jp
operator|=
name|jsonFactory
operator|.
name|createJsonParser
argument_list|(
name|source
argument_list|)
expr_stmt|;
name|jsonContext
operator|.
name|reset
argument_list|(
name|jp
argument_list|,
operator|new
name|Document
argument_list|()
argument_list|,
name|type
argument_list|,
name|source
argument_list|,
name|listener
argument_list|)
expr_stmt|;
comment|// will result in JsonToken.START_OBJECT
name|JsonToken
name|token
init|=
name|jp
operator|.
name|nextToken
argument_list|()
decl_stmt|;
if|if
condition|(
name|token
operator|!=
name|JsonToken
operator|.
name|START_OBJECT
condition|)
block|{
throw|throw
operator|new
name|MapperException
argument_list|(
literal|"Malformed json, must start with an object"
argument_list|)
throw|;
block|}
name|token
operator|=
name|jp
operator|.
name|nextToken
argument_list|()
expr_stmt|;
if|if
condition|(
name|token
operator|!=
name|JsonToken
operator|.
name|FIELD_NAME
condition|)
block|{
throw|throw
operator|new
name|MapperException
argument_list|(
literal|"Malformed json, after first object, the type name must exists"
argument_list|)
throw|;
block|}
if|if
condition|(
operator|!
name|jp
operator|.
name|getCurrentName
argument_list|()
operator|.
name|equals
argument_list|(
name|type
argument_list|)
condition|)
block|{
if|if
condition|(
name|type
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|MapperException
argument_list|(
literal|"Json content type ["
operator|+
name|jp
operator|.
name|getCurrentName
argument_list|()
operator|+
literal|"] does not match the type of the mapper ["
operator|+
name|type
operator|+
literal|"]"
argument_list|)
throw|;
block|}
comment|// continue
block|}
else|else
block|{
comment|// now move to the actual content, which is the start object
name|token
operator|=
name|jp
operator|.
name|nextToken
argument_list|()
expr_stmt|;
if|if
condition|(
name|token
operator|!=
name|JsonToken
operator|.
name|START_OBJECT
condition|)
block|{
throw|throw
operator|new
name|MapperException
argument_list|(
literal|"Malformed json, after type is must start with an object"
argument_list|)
throw|;
block|}
block|}
if|if
condition|(
name|sourceFieldMapper
operator|.
name|enabled
argument_list|()
condition|)
block|{
name|sourceFieldMapper
operator|.
name|parse
argument_list|(
name|jsonContext
argument_list|)
expr_stmt|;
block|}
comment|// set the id if we have it so we can validate it later on, also, add the uid if we can
if|if
condition|(
name|id
operator|!=
literal|null
condition|)
block|{
name|jsonContext
operator|.
name|id
argument_list|(
name|id
argument_list|)
expr_stmt|;
name|uidFieldMapper
operator|.
name|parse
argument_list|(
name|jsonContext
argument_list|)
expr_stmt|;
block|}
name|typeFieldMapper
operator|.
name|parse
argument_list|(
name|jsonContext
argument_list|)
expr_stmt|;
name|rootObjectMapper
operator|.
name|parse
argument_list|(
name|jsonContext
argument_list|)
expr_stmt|;
comment|// if we did not get the id, we need to parse the uid into the document now, after it was added
if|if
condition|(
name|id
operator|==
literal|null
condition|)
block|{
name|uidFieldMapper
operator|.
name|parse
argument_list|(
name|jsonContext
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|jsonContext
operator|.
name|parsedIdState
argument_list|()
operator|!=
name|JsonParseContext
operator|.
name|ParsedIdState
operator|.
name|PARSED
condition|)
block|{
comment|// mark it as external, so we can parse it
name|jsonContext
operator|.
name|parsedId
argument_list|(
name|JsonParseContext
operator|.
name|ParsedIdState
operator|.
name|EXTERNAL
argument_list|)
expr_stmt|;
name|idFieldMapper
operator|.
name|parse
argument_list|(
name|jsonContext
argument_list|)
expr_stmt|;
block|}
name|allFieldMapper
operator|.
name|parse
argument_list|(
name|jsonContext
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|MapperParsingException
argument_list|(
literal|"Failed to parse"
argument_list|,
name|e
argument_list|)
throw|;
block|}
finally|finally
block|{
if|if
condition|(
name|jp
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|jp
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
comment|// ignore
block|}
block|}
block|}
return|return
operator|new
name|ParsedDocument
argument_list|(
name|jsonContext
operator|.
name|uid
argument_list|()
argument_list|,
name|jsonContext
operator|.
name|id
argument_list|()
argument_list|,
name|jsonContext
operator|.
name|type
argument_list|()
argument_list|,
name|jsonContext
operator|.
name|doc
argument_list|()
argument_list|,
name|source
argument_list|,
name|jsonContext
operator|.
name|mappersAdded
argument_list|()
argument_list|)
return|;
block|}
DECL|method|addFieldMapper
name|void
name|addFieldMapper
parameter_list|(
name|FieldMapper
name|fieldMapper
parameter_list|)
block|{
synchronized|synchronized
init|(
name|mutex
init|)
block|{
name|fieldMappers
operator|=
name|fieldMappers
operator|.
name|concat
argument_list|(
name|this
argument_list|,
name|fieldMapper
argument_list|)
expr_stmt|;
for|for
control|(
name|FieldMapperListener
name|listener
range|:
name|fieldMapperListeners
control|)
block|{
name|listener
operator|.
name|fieldMapper
argument_list|(
name|fieldMapper
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|method|addFieldMapperListener
annotation|@
name|Override
specifier|public
name|void
name|addFieldMapperListener
parameter_list|(
name|FieldMapperListener
name|fieldMapperListener
parameter_list|,
name|boolean
name|includeExisting
parameter_list|)
block|{
synchronized|synchronized
init|(
name|mutex
init|)
block|{
name|fieldMapperListeners
operator|.
name|add
argument_list|(
name|fieldMapperListener
argument_list|)
expr_stmt|;
if|if
condition|(
name|includeExisting
condition|)
block|{
name|fieldMapperListener
operator|.
name|fieldMapper
argument_list|(
name|sourceFieldMapper
argument_list|)
expr_stmt|;
name|fieldMapperListener
operator|.
name|fieldMapper
argument_list|(
name|typeFieldMapper
argument_list|)
expr_stmt|;
name|fieldMapperListener
operator|.
name|fieldMapper
argument_list|(
name|idFieldMapper
argument_list|)
expr_stmt|;
name|fieldMapperListener
operator|.
name|fieldMapper
argument_list|(
name|uidFieldMapper
argument_list|)
expr_stmt|;
name|fieldMapperListener
operator|.
name|fieldMapper
argument_list|(
name|allFieldMapper
argument_list|)
expr_stmt|;
name|rootObjectMapper
operator|.
name|traverse
argument_list|(
name|fieldMapperListener
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|method|merge
annotation|@
name|Override
specifier|public
specifier|synchronized
name|MergeResult
name|merge
parameter_list|(
name|DocumentMapper
name|mergeWith
parameter_list|,
name|MergeFlags
name|mergeFlags
parameter_list|)
block|{
name|JsonDocumentMapper
name|jsonMergeWith
init|=
operator|(
name|JsonDocumentMapper
operator|)
name|mergeWith
decl_stmt|;
name|JsonMergeContext
name|mergeContext
init|=
operator|new
name|JsonMergeContext
argument_list|(
name|this
argument_list|,
name|mergeFlags
argument_list|)
decl_stmt|;
name|rootObjectMapper
operator|.
name|merge
argument_list|(
name|jsonMergeWith
operator|.
name|rootObjectMapper
argument_list|,
name|mergeContext
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|mergeFlags
operator|.
name|simulate
argument_list|()
condition|)
block|{
comment|// update the source to the merged one
name|mappingSource
operator|=
name|buildSource
argument_list|()
expr_stmt|;
block|}
return|return
operator|new
name|MergeResult
argument_list|(
name|mergeContext
operator|.
name|buildConflicts
argument_list|()
argument_list|)
return|;
block|}
DECL|method|buildSource
annotation|@
name|Override
specifier|public
name|String
name|buildSource
parameter_list|()
throws|throws
name|FailedToGenerateSourceMapperException
block|{
try|try
block|{
name|StringJsonBuilder
name|builder
init|=
name|stringJsonBuilder
argument_list|()
operator|.
name|prettyPrint
argument_list|()
decl_stmt|;
name|builder
operator|.
name|startObject
argument_list|()
expr_stmt|;
name|toJson
argument_list|(
name|builder
argument_list|,
name|ToJson
operator|.
name|EMPTY_PARAMS
argument_list|)
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
return|return
name|builder
operator|.
name|string
argument_list|()
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
name|FailedToGenerateSourceMapperException
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
DECL|method|toJson
annotation|@
name|Override
specifier|public
name|void
name|toJson
parameter_list|(
name|JsonBuilder
name|builder
parameter_list|,
name|Params
name|params
parameter_list|)
throws|throws
name|IOException
block|{
name|rootObjectMapper
operator|.
name|toJson
argument_list|(
name|builder
argument_list|,
name|params
argument_list|,
name|allFieldMapper
argument_list|,
name|sourceFieldMapper
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

