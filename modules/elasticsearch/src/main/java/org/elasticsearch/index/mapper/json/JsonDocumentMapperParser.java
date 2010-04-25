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
name|elasticsearch
operator|.
name|util
operator|.
name|gcommon
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
name|codehaus
operator|.
name|jackson
operator|.
name|JsonNode
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
name|map
operator|.
name|ObjectMapper
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
name|node
operator|.
name|ObjectNode
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
name|AnalysisService
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
name|DocumentMapperParser
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
name|MapBuilder
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
name|Strings
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
name|io
operator|.
name|FastStringReader
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
name|index
operator|.
name|mapper
operator|.
name|json
operator|.
name|JsonMapperBuilders
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
name|index
operator|.
name|mapper
operator|.
name|json
operator|.
name|JsonTypeParsers
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
name|JacksonNodes
operator|.
name|*
import|;
end_import

begin_comment
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|JsonDocumentMapperParser
specifier|public
class|class
name|JsonDocumentMapperParser
implements|implements
name|DocumentMapperParser
block|{
DECL|field|objectMapper
specifier|private
specifier|final
name|ObjectMapper
name|objectMapper
init|=
name|Jackson
operator|.
name|newObjectMapper
argument_list|()
decl_stmt|;
DECL|field|analysisService
specifier|private
specifier|final
name|AnalysisService
name|analysisService
decl_stmt|;
DECL|field|rootObjectTypeParser
specifier|private
specifier|final
name|JsonObjectMapper
operator|.
name|TypeParser
name|rootObjectTypeParser
init|=
operator|new
name|JsonObjectMapper
operator|.
name|TypeParser
argument_list|()
decl_stmt|;
DECL|field|typeParsersMutex
specifier|private
specifier|final
name|Object
name|typeParsersMutex
init|=
operator|new
name|Object
argument_list|()
decl_stmt|;
DECL|field|typeParsers
specifier|private
specifier|volatile
name|ImmutableMap
argument_list|<
name|String
argument_list|,
name|JsonTypeParser
argument_list|>
name|typeParsers
decl_stmt|;
DECL|method|JsonDocumentMapperParser
specifier|public
name|JsonDocumentMapperParser
parameter_list|(
name|AnalysisService
name|analysisService
parameter_list|)
block|{
name|this
operator|.
name|analysisService
operator|=
name|analysisService
expr_stmt|;
name|typeParsers
operator|=
operator|new
name|MapBuilder
argument_list|<
name|String
argument_list|,
name|JsonTypeParser
argument_list|>
argument_list|()
operator|.
name|put
argument_list|(
name|JsonShortFieldMapper
operator|.
name|JSON_TYPE
argument_list|,
operator|new
name|JsonShortFieldMapper
operator|.
name|TypeParser
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
name|JsonIntegerFieldMapper
operator|.
name|JSON_TYPE
argument_list|,
operator|new
name|JsonIntegerFieldMapper
operator|.
name|TypeParser
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
name|JsonLongFieldMapper
operator|.
name|JSON_TYPE
argument_list|,
operator|new
name|JsonLongFieldMapper
operator|.
name|TypeParser
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
name|JsonFloatFieldMapper
operator|.
name|JSON_TYPE
argument_list|,
operator|new
name|JsonFloatFieldMapper
operator|.
name|TypeParser
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
name|JsonDoubleFieldMapper
operator|.
name|JSON_TYPE
argument_list|,
operator|new
name|JsonDoubleFieldMapper
operator|.
name|TypeParser
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
name|JsonBooleanFieldMapper
operator|.
name|JSON_TYPE
argument_list|,
operator|new
name|JsonBooleanFieldMapper
operator|.
name|TypeParser
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
name|JsonBinaryFieldMapper
operator|.
name|JSON_TYPE
argument_list|,
operator|new
name|JsonBinaryFieldMapper
operator|.
name|TypeParser
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
name|JsonDateFieldMapper
operator|.
name|JSON_TYPE
argument_list|,
operator|new
name|JsonDateFieldMapper
operator|.
name|TypeParser
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
name|JsonStringFieldMapper
operator|.
name|JSON_TYPE
argument_list|,
operator|new
name|JsonStringFieldMapper
operator|.
name|TypeParser
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
name|JsonObjectMapper
operator|.
name|JSON_TYPE
argument_list|,
operator|new
name|JsonObjectMapper
operator|.
name|TypeParser
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
name|JsonMultiFieldMapper
operator|.
name|JSON_TYPE
argument_list|,
operator|new
name|JsonMultiFieldMapper
operator|.
name|TypeParser
argument_list|()
argument_list|)
operator|.
name|immutableMap
argument_list|()
expr_stmt|;
block|}
DECL|method|putTypeParser
specifier|public
name|void
name|putTypeParser
parameter_list|(
name|String
name|type
parameter_list|,
name|JsonTypeParser
name|typeParser
parameter_list|)
block|{
synchronized|synchronized
init|(
name|typeParsersMutex
init|)
block|{
name|typeParsers
operator|=
operator|new
name|MapBuilder
argument_list|<
name|String
argument_list|,
name|JsonTypeParser
argument_list|>
argument_list|()
operator|.
name|putAll
argument_list|(
name|typeParsers
argument_list|)
operator|.
name|put
argument_list|(
name|type
argument_list|,
name|typeParser
argument_list|)
operator|.
name|immutableMap
argument_list|()
expr_stmt|;
block|}
block|}
DECL|method|parse
annotation|@
name|Override
specifier|public
name|DocumentMapper
name|parse
parameter_list|(
name|String
name|source
parameter_list|)
throws|throws
name|MapperParsingException
block|{
return|return
name|parse
argument_list|(
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
name|DocumentMapper
name|parse
parameter_list|(
name|String
name|type
parameter_list|,
name|String
name|source
parameter_list|)
throws|throws
name|MapperParsingException
block|{
name|JsonNode
name|root
decl_stmt|;
try|try
block|{
name|root
operator|=
name|objectMapper
operator|.
name|readValue
argument_list|(
operator|new
name|FastStringReader
argument_list|(
name|source
argument_list|)
argument_list|,
name|JsonNode
operator|.
name|class
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
literal|"Failed to parse json mapping definition"
argument_list|,
name|e
argument_list|)
throw|;
block|}
name|String
name|rootName
init|=
name|root
operator|.
name|getFieldNames
argument_list|()
operator|.
name|next
argument_list|()
decl_stmt|;
name|ObjectNode
name|rootObj
decl_stmt|;
if|if
condition|(
name|type
operator|==
literal|null
condition|)
block|{
comment|// we have no type, we assume the first node is the type
name|rootObj
operator|=
operator|(
name|ObjectNode
operator|)
name|root
operator|.
name|get
argument_list|(
name|rootName
argument_list|)
expr_stmt|;
name|type
operator|=
name|rootName
expr_stmt|;
block|}
else|else
block|{
comment|// we have a type, check if the top level one is the type as well
comment|// if it is, then the root is that node, if not then the root is the master node
if|if
condition|(
name|type
operator|.
name|equals
argument_list|(
name|rootName
argument_list|)
condition|)
block|{
name|JsonNode
name|tmpNode
init|=
name|root
operator|.
name|get
argument_list|(
name|type
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|tmpNode
operator|.
name|isObject
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|MapperParsingException
argument_list|(
literal|"Expected root node name ["
operator|+
name|rootName
operator|+
literal|"] to be of json object type, but its not"
argument_list|)
throw|;
block|}
name|rootObj
operator|=
operator|(
name|ObjectNode
operator|)
name|tmpNode
expr_stmt|;
block|}
else|else
block|{
name|rootObj
operator|=
operator|(
name|ObjectNode
operator|)
name|root
expr_stmt|;
block|}
block|}
name|JsonTypeParser
operator|.
name|ParserContext
name|parserContext
init|=
operator|new
name|JsonTypeParser
operator|.
name|ParserContext
argument_list|(
name|rootObj
argument_list|,
name|analysisService
argument_list|,
name|typeParsers
argument_list|)
decl_stmt|;
name|JsonDocumentMapper
operator|.
name|Builder
name|docBuilder
init|=
name|doc
argument_list|(
operator|(
name|JsonObjectMapper
operator|.
name|Builder
operator|)
name|rootObjectTypeParser
operator|.
name|parse
argument_list|(
name|type
argument_list|,
name|rootObj
argument_list|,
name|parserContext
argument_list|)
argument_list|)
decl_stmt|;
for|for
control|(
name|Iterator
argument_list|<
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|JsonNode
argument_list|>
argument_list|>
name|fieldsIt
init|=
name|rootObj
operator|.
name|getFields
argument_list|()
init|;
name|fieldsIt
operator|.
name|hasNext
argument_list|()
condition|;
control|)
block|{
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|JsonNode
argument_list|>
name|entry
init|=
name|fieldsIt
operator|.
name|next
argument_list|()
decl_stmt|;
name|String
name|fieldName
init|=
name|Strings
operator|.
name|toUnderscoreCase
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
decl_stmt|;
name|JsonNode
name|fieldNode
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
if|if
condition|(
name|JsonSourceFieldMapper
operator|.
name|JSON_TYPE
operator|.
name|equals
argument_list|(
name|fieldName
argument_list|)
operator|||
literal|"sourceField"
operator|.
name|equals
argument_list|(
name|fieldName
argument_list|)
condition|)
block|{
name|docBuilder
operator|.
name|sourceField
argument_list|(
name|parseSourceField
argument_list|(
operator|(
name|ObjectNode
operator|)
name|fieldNode
argument_list|,
name|parserContext
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|JsonIdFieldMapper
operator|.
name|JSON_TYPE
operator|.
name|equals
argument_list|(
name|fieldName
argument_list|)
operator|||
literal|"idField"
operator|.
name|equals
argument_list|(
name|fieldName
argument_list|)
condition|)
block|{
name|docBuilder
operator|.
name|idField
argument_list|(
name|parseIdField
argument_list|(
operator|(
name|ObjectNode
operator|)
name|fieldNode
argument_list|,
name|parserContext
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|JsonTypeFieldMapper
operator|.
name|JSON_TYPE
operator|.
name|equals
argument_list|(
name|fieldName
argument_list|)
operator|||
literal|"typeField"
operator|.
name|equals
argument_list|(
name|fieldName
argument_list|)
condition|)
block|{
name|docBuilder
operator|.
name|typeField
argument_list|(
name|parseTypeField
argument_list|(
operator|(
name|ObjectNode
operator|)
name|fieldNode
argument_list|,
name|parserContext
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|JsonUidFieldMapper
operator|.
name|JSON_TYPE
operator|.
name|equals
argument_list|(
name|fieldName
argument_list|)
operator|||
literal|"uidField"
operator|.
name|equals
argument_list|(
name|fieldName
argument_list|)
condition|)
block|{
name|docBuilder
operator|.
name|uidField
argument_list|(
name|parseUidField
argument_list|(
operator|(
name|ObjectNode
operator|)
name|fieldNode
argument_list|,
name|parserContext
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|JsonBoostFieldMapper
operator|.
name|JSON_TYPE
operator|.
name|equals
argument_list|(
name|fieldName
argument_list|)
operator|||
literal|"boostField"
operator|.
name|equals
argument_list|(
name|fieldName
argument_list|)
condition|)
block|{
name|docBuilder
operator|.
name|boostField
argument_list|(
name|parseBoostField
argument_list|(
operator|(
name|ObjectNode
operator|)
name|fieldNode
argument_list|,
name|parserContext
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|JsonAllFieldMapper
operator|.
name|JSON_TYPE
operator|.
name|equals
argument_list|(
name|fieldName
argument_list|)
operator|||
literal|"allField"
operator|.
name|equals
argument_list|(
name|fieldName
argument_list|)
condition|)
block|{
name|docBuilder
operator|.
name|allField
argument_list|(
name|parseAllField
argument_list|(
operator|(
name|ObjectNode
operator|)
name|fieldNode
argument_list|,
name|parserContext
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"index_analyzer"
operator|.
name|equals
argument_list|(
name|fieldName
argument_list|)
condition|)
block|{
name|docBuilder
operator|.
name|indexAnalyzer
argument_list|(
name|analysisService
operator|.
name|analyzer
argument_list|(
name|fieldNode
operator|.
name|getTextValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"search_analyzer"
operator|.
name|equals
argument_list|(
name|fieldName
argument_list|)
condition|)
block|{
name|docBuilder
operator|.
name|searchAnalyzer
argument_list|(
name|analysisService
operator|.
name|analyzer
argument_list|(
name|fieldNode
operator|.
name|getTextValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"analyzer"
operator|.
name|equals
argument_list|(
name|fieldName
argument_list|)
condition|)
block|{
name|docBuilder
operator|.
name|indexAnalyzer
argument_list|(
name|analysisService
operator|.
name|analyzer
argument_list|(
name|fieldNode
operator|.
name|getTextValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|docBuilder
operator|.
name|searchAnalyzer
argument_list|(
name|analysisService
operator|.
name|analyzer
argument_list|(
name|fieldNode
operator|.
name|getTextValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
operator|!
name|docBuilder
operator|.
name|hasIndexAnalyzer
argument_list|()
condition|)
block|{
name|docBuilder
operator|.
name|indexAnalyzer
argument_list|(
name|analysisService
operator|.
name|defaultIndexAnalyzer
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|docBuilder
operator|.
name|hasSearchAnalyzer
argument_list|()
condition|)
block|{
name|docBuilder
operator|.
name|searchAnalyzer
argument_list|(
name|analysisService
operator|.
name|defaultSearchAnalyzer
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|docBuilder
operator|.
name|mappingSource
argument_list|(
name|source
argument_list|)
expr_stmt|;
name|JsonDocumentMapper
name|documentMapper
init|=
name|docBuilder
operator|.
name|build
argument_list|()
decl_stmt|;
comment|// update the source with the generated one
name|documentMapper
operator|.
name|mappingSource
argument_list|(
name|documentMapper
operator|.
name|buildSource
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|documentMapper
return|;
block|}
DECL|method|parseUidField
specifier|private
name|JsonUidFieldMapper
operator|.
name|Builder
name|parseUidField
parameter_list|(
name|ObjectNode
name|uidNode
parameter_list|,
name|JsonTypeParser
operator|.
name|ParserContext
name|parserContext
parameter_list|)
block|{
comment|//        String name = uidNode.get("name") == null ? JsonUidFieldMapper.Defaults.NAME : uidNode.get("name").getTextValue();
name|JsonUidFieldMapper
operator|.
name|Builder
name|builder
init|=
name|uid
argument_list|()
decl_stmt|;
comment|//        for (Iterator<Map.Entry<String, JsonNode>> fieldsIt = uidNode.getFields(); fieldsIt.hasNext();) {
comment|//            Map.Entry<String, JsonNode> entry = fieldsIt.next();
comment|//            String fieldName = entry.getKey();
comment|//            JsonNode fieldNode = entry.getValue();
comment|//
comment|//            if ("indexName".equals(fieldName)) {
comment|//                builder.indexName(fieldNode.getTextValue());
comment|//            }
comment|//        }
return|return
name|builder
return|;
block|}
DECL|method|parseBoostField
specifier|private
name|JsonBoostFieldMapper
operator|.
name|Builder
name|parseBoostField
parameter_list|(
name|ObjectNode
name|boostNode
parameter_list|,
name|JsonTypeParser
operator|.
name|ParserContext
name|parserContext
parameter_list|)
block|{
name|String
name|name
init|=
name|boostNode
operator|.
name|get
argument_list|(
literal|"name"
argument_list|)
operator|==
literal|null
condition|?
name|JsonBoostFieldMapper
operator|.
name|Defaults
operator|.
name|NAME
else|:
name|boostNode
operator|.
name|get
argument_list|(
literal|"name"
argument_list|)
operator|.
name|getTextValue
argument_list|()
decl_stmt|;
name|JsonBoostFieldMapper
operator|.
name|Builder
name|builder
init|=
name|boost
argument_list|(
name|name
argument_list|)
decl_stmt|;
name|parseNumberField
argument_list|(
name|builder
argument_list|,
name|name
argument_list|,
name|boostNode
argument_list|,
name|parserContext
argument_list|)
expr_stmt|;
for|for
control|(
name|Iterator
argument_list|<
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|JsonNode
argument_list|>
argument_list|>
name|propsIt
init|=
name|boostNode
operator|.
name|getFields
argument_list|()
init|;
name|propsIt
operator|.
name|hasNext
argument_list|()
condition|;
control|)
block|{
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|JsonNode
argument_list|>
name|entry
init|=
name|propsIt
operator|.
name|next
argument_list|()
decl_stmt|;
name|String
name|propName
init|=
name|Strings
operator|.
name|toUnderscoreCase
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
decl_stmt|;
name|JsonNode
name|propNode
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
if|if
condition|(
name|propName
operator|.
name|equals
argument_list|(
literal|"null_value"
argument_list|)
condition|)
block|{
name|builder
operator|.
name|nullValue
argument_list|(
name|nodeFloatValue
argument_list|(
name|propNode
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|builder
return|;
block|}
DECL|method|parseTypeField
specifier|private
name|JsonTypeFieldMapper
operator|.
name|Builder
name|parseTypeField
parameter_list|(
name|ObjectNode
name|typeNode
parameter_list|,
name|JsonTypeParser
operator|.
name|ParserContext
name|parserContext
parameter_list|)
block|{
comment|//        String name = typeNode.get("name") == null ? JsonTypeFieldMapper.Defaults.NAME : typeNode.get("name").getTextValue();
name|JsonTypeFieldMapper
operator|.
name|Builder
name|builder
init|=
name|type
argument_list|()
decl_stmt|;
name|parseJsonField
argument_list|(
name|builder
argument_list|,
name|builder
operator|.
name|name
argument_list|,
name|typeNode
argument_list|,
name|parserContext
argument_list|)
expr_stmt|;
return|return
name|builder
return|;
block|}
DECL|method|parseIdField
specifier|private
name|JsonIdFieldMapper
operator|.
name|Builder
name|parseIdField
parameter_list|(
name|ObjectNode
name|idNode
parameter_list|,
name|JsonTypeParser
operator|.
name|ParserContext
name|parserContext
parameter_list|)
block|{
comment|//        String name = idNode.get("name") == null ? JsonIdFieldMapper.Defaults.NAME : idNode.get("name").getTextValue();
name|JsonIdFieldMapper
operator|.
name|Builder
name|builder
init|=
name|id
argument_list|()
decl_stmt|;
name|parseJsonField
argument_list|(
name|builder
argument_list|,
name|builder
operator|.
name|name
argument_list|,
name|idNode
argument_list|,
name|parserContext
argument_list|)
expr_stmt|;
return|return
name|builder
return|;
block|}
DECL|method|parseAllField
specifier|private
name|JsonAllFieldMapper
operator|.
name|Builder
name|parseAllField
parameter_list|(
name|ObjectNode
name|allNode
parameter_list|,
name|JsonTypeParser
operator|.
name|ParserContext
name|parserContext
parameter_list|)
block|{
comment|//        String name = idNode.get("name") == null ? JsonIdFieldMapper.Defaults.NAME : idNode.get("name").getTextValue();
name|JsonAllFieldMapper
operator|.
name|Builder
name|builder
init|=
name|all
argument_list|()
decl_stmt|;
name|parseJsonField
argument_list|(
name|builder
argument_list|,
name|builder
operator|.
name|name
argument_list|,
name|allNode
argument_list|,
name|parserContext
argument_list|)
expr_stmt|;
for|for
control|(
name|Iterator
argument_list|<
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|JsonNode
argument_list|>
argument_list|>
name|fieldsIt
init|=
name|allNode
operator|.
name|getFields
argument_list|()
init|;
name|fieldsIt
operator|.
name|hasNext
argument_list|()
condition|;
control|)
block|{
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|JsonNode
argument_list|>
name|entry
init|=
name|fieldsIt
operator|.
name|next
argument_list|()
decl_stmt|;
name|String
name|fieldName
init|=
name|Strings
operator|.
name|toUnderscoreCase
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
decl_stmt|;
name|JsonNode
name|fieldNode
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
if|if
condition|(
name|fieldName
operator|.
name|equals
argument_list|(
literal|"enabled"
argument_list|)
condition|)
block|{
name|builder
operator|.
name|enabled
argument_list|(
name|nodeBooleanValue
argument_list|(
name|fieldNode
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|builder
return|;
block|}
DECL|method|parseSourceField
specifier|private
name|JsonSourceFieldMapper
operator|.
name|Builder
name|parseSourceField
parameter_list|(
name|ObjectNode
name|sourceNode
parameter_list|,
name|JsonTypeParser
operator|.
name|ParserContext
name|parserContext
parameter_list|)
block|{
comment|//        String name = sourceNode.get("name") == null ? JsonSourceFieldMapper.Defaults.NAME : sourceNode.get("name").getTextValue();
name|JsonSourceFieldMapper
operator|.
name|Builder
name|builder
init|=
name|source
argument_list|()
decl_stmt|;
for|for
control|(
name|Iterator
argument_list|<
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|JsonNode
argument_list|>
argument_list|>
name|fieldsIt
init|=
name|sourceNode
operator|.
name|getFields
argument_list|()
init|;
name|fieldsIt
operator|.
name|hasNext
argument_list|()
condition|;
control|)
block|{
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|JsonNode
argument_list|>
name|entry
init|=
name|fieldsIt
operator|.
name|next
argument_list|()
decl_stmt|;
name|String
name|fieldName
init|=
name|Strings
operator|.
name|toUnderscoreCase
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
decl_stmt|;
name|JsonNode
name|fieldNode
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
if|if
condition|(
name|fieldName
operator|.
name|equals
argument_list|(
literal|"enabled"
argument_list|)
condition|)
block|{
name|builder
operator|.
name|enabled
argument_list|(
name|nodeBooleanValue
argument_list|(
name|fieldNode
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|//            if (fieldName.equals("compressionThreshold")) {
comment|//                builder.compressionThreshold(nodeIn...);
comment|//            } else if (fieldName.equals("compressionType")) {
comment|//                String compressionType = fieldNode.getTextValue();
comment|//                if ("zip".equals(compressionType)) {
comment|//                    builder.compressor(new ZipCompressor());
comment|//                } else if ("gzip".equals(compressionType)) {
comment|//                    builder.compressor(new GZIPCompressor());
comment|//                } else if ("lzf".equals(compressionType)) {
comment|//                    builder.compressor(new LzfCompressor());
comment|//                } else {
comment|//                    throw new MapperParsingException("No compressor registed under [" + compressionType + "]");
comment|//                }
comment|//            }
block|}
return|return
name|builder
return|;
block|}
block|}
end_class

end_unit

