begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.query.json
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|query
operator|.
name|json
package|;
end_package

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|inject
operator|.
name|Inject
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
name|search
operator|.
name|Filter
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
name|search
operator|.
name|TermRangeFilter
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
name|AbstractIndexComponent
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
name|Index
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
name|MapperService
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
name|query
operator|.
name|QueryParsingException
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
name|settings
operator|.
name|IndexSettings
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
name|Booleans
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
name|settings
operator|.
name|Settings
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
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|query
operator|.
name|support
operator|.
name|QueryParsers
operator|.
name|*
import|;
end_import

begin_comment
comment|/**  * @author kimchy (Shay Banon)  */
end_comment

begin_class
DECL|class|RangeJsonFilterParser
specifier|public
class|class
name|RangeJsonFilterParser
extends|extends
name|AbstractIndexComponent
implements|implements
name|JsonFilterParser
block|{
DECL|field|NAME
specifier|public
specifier|static
specifier|final
name|String
name|NAME
init|=
literal|"range"
decl_stmt|;
DECL|method|RangeJsonFilterParser
annotation|@
name|Inject
specifier|public
name|RangeJsonFilterParser
parameter_list|(
name|Index
name|index
parameter_list|,
annotation|@
name|IndexSettings
name|Settings
name|settings
parameter_list|)
block|{
name|super
argument_list|(
name|index
argument_list|,
name|settings
argument_list|)
expr_stmt|;
block|}
DECL|method|name
annotation|@
name|Override
specifier|public
name|String
name|name
parameter_list|()
block|{
return|return
name|NAME
return|;
block|}
DECL|method|parse
annotation|@
name|Override
specifier|public
name|Filter
name|parse
parameter_list|(
name|JsonQueryParseContext
name|parseContext
parameter_list|)
throws|throws
name|IOException
throws|,
name|QueryParsingException
block|{
name|JsonParser
name|jp
init|=
name|parseContext
operator|.
name|jp
argument_list|()
decl_stmt|;
name|JsonToken
name|token
init|=
name|jp
operator|.
name|nextToken
argument_list|()
decl_stmt|;
assert|assert
name|token
operator|==
name|JsonToken
operator|.
name|FIELD_NAME
assert|;
name|String
name|fieldName
init|=
name|jp
operator|.
name|getCurrentName
argument_list|()
decl_stmt|;
comment|// now, we move after the field name, which starts the object
name|token
operator|=
name|jp
operator|.
name|nextToken
argument_list|()
expr_stmt|;
assert|assert
name|token
operator|==
name|JsonToken
operator|.
name|START_OBJECT
assert|;
name|String
name|from
init|=
literal|null
decl_stmt|;
name|String
name|to
init|=
literal|null
decl_stmt|;
name|boolean
name|includeLower
init|=
literal|true
decl_stmt|;
name|boolean
name|includeUpper
init|=
literal|true
decl_stmt|;
name|String
name|currentFieldName
init|=
literal|null
decl_stmt|;
while|while
condition|(
operator|(
name|token
operator|=
name|jp
operator|.
name|nextToken
argument_list|()
operator|)
operator|!=
name|JsonToken
operator|.
name|END_OBJECT
condition|)
block|{
if|if
condition|(
name|token
operator|==
name|JsonToken
operator|.
name|FIELD_NAME
condition|)
block|{
name|currentFieldName
operator|=
name|jp
operator|.
name|getCurrentName
argument_list|()
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
literal|"from"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
if|if
condition|(
name|jp
operator|.
name|getCurrentToken
argument_list|()
operator|==
name|JsonToken
operator|.
name|VALUE_NULL
condition|)
block|{
name|from
operator|=
literal|null
expr_stmt|;
block|}
else|else
block|{
name|from
operator|=
name|jp
operator|.
name|getText
argument_list|()
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
literal|"to"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
if|if
condition|(
name|jp
operator|.
name|getCurrentToken
argument_list|()
operator|==
name|JsonToken
operator|.
name|VALUE_NULL
condition|)
block|{
name|to
operator|=
literal|null
expr_stmt|;
block|}
else|else
block|{
name|to
operator|=
name|jp
operator|.
name|getText
argument_list|()
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
literal|"include_lower"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
if|if
condition|(
name|token
operator|==
name|JsonToken
operator|.
name|VALUE_NUMBER_INT
condition|)
block|{
name|includeLower
operator|=
name|jp
operator|.
name|getIntValue
argument_list|()
operator|!=
literal|0
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|token
operator|==
name|JsonToken
operator|.
name|VALUE_STRING
condition|)
block|{
name|includeLower
operator|=
name|Booleans
operator|.
name|parseBoolean
argument_list|(
name|jp
operator|.
name|getText
argument_list|()
argument_list|,
name|includeLower
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|includeLower
operator|=
name|token
operator|==
name|JsonToken
operator|.
name|VALUE_TRUE
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
literal|"include_upper"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
if|if
condition|(
name|token
operator|==
name|JsonToken
operator|.
name|VALUE_NUMBER_INT
condition|)
block|{
name|includeUpper
operator|=
name|jp
operator|.
name|getIntValue
argument_list|()
operator|!=
literal|0
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|token
operator|==
name|JsonToken
operator|.
name|VALUE_STRING
condition|)
block|{
name|includeUpper
operator|=
name|Booleans
operator|.
name|parseBoolean
argument_list|(
name|jp
operator|.
name|getText
argument_list|()
argument_list|,
name|includeUpper
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|includeUpper
operator|=
name|token
operator|==
name|JsonToken
operator|.
name|VALUE_TRUE
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
literal|"gte"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
if|if
condition|(
name|jp
operator|.
name|getCurrentToken
argument_list|()
operator|==
name|JsonToken
operator|.
name|VALUE_NULL
condition|)
block|{
name|from
operator|=
literal|null
expr_stmt|;
block|}
else|else
block|{
name|from
operator|=
name|jp
operator|.
name|getText
argument_list|()
expr_stmt|;
block|}
name|includeLower
operator|=
literal|true
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"lt"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
if|if
condition|(
name|jp
operator|.
name|getCurrentToken
argument_list|()
operator|==
name|JsonToken
operator|.
name|VALUE_NULL
condition|)
block|{
name|to
operator|=
literal|null
expr_stmt|;
block|}
else|else
block|{
name|to
operator|=
name|jp
operator|.
name|getText
argument_list|()
expr_stmt|;
block|}
name|includeUpper
operator|=
literal|false
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"lte"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
if|if
condition|(
name|jp
operator|.
name|getCurrentToken
argument_list|()
operator|==
name|JsonToken
operator|.
name|VALUE_NULL
condition|)
block|{
name|to
operator|=
literal|null
expr_stmt|;
block|}
else|else
block|{
name|to
operator|=
name|jp
operator|.
name|getText
argument_list|()
expr_stmt|;
block|}
name|includeUpper
operator|=
literal|true
expr_stmt|;
block|}
block|}
block|}
comment|// move to the next end object, to close the field name
name|token
operator|=
name|jp
operator|.
name|nextToken
argument_list|()
expr_stmt|;
assert|assert
name|token
operator|==
name|JsonToken
operator|.
name|END_OBJECT
assert|;
name|Filter
name|filter
init|=
literal|null
decl_stmt|;
name|MapperService
operator|.
name|SmartNameFieldMappers
name|smartNameFieldMappers
init|=
name|parseContext
operator|.
name|smartFieldMappers
argument_list|(
name|fieldName
argument_list|)
decl_stmt|;
if|if
condition|(
name|smartNameFieldMappers
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|smartNameFieldMappers
operator|.
name|hasMapper
argument_list|()
condition|)
block|{
name|filter
operator|=
name|smartNameFieldMappers
operator|.
name|mapper
argument_list|()
operator|.
name|rangeFilter
argument_list|(
name|from
argument_list|,
name|to
argument_list|,
name|includeLower
argument_list|,
name|includeUpper
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|filter
operator|==
literal|null
condition|)
block|{
name|filter
operator|=
operator|new
name|TermRangeFilter
argument_list|(
name|fieldName
argument_list|,
name|from
argument_list|,
name|to
argument_list|,
name|includeLower
argument_list|,
name|includeUpper
argument_list|)
expr_stmt|;
block|}
return|return
name|wrapSmartNameFilter
argument_list|(
name|filter
argument_list|,
name|smartNameFieldMappers
argument_list|,
name|parseContext
operator|.
name|indexCache
argument_list|()
argument_list|)
return|;
block|}
block|}
end_class

end_unit

