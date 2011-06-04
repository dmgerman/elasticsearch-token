begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.query
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|query
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
name|search
operator|.
name|Filter
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
name|inject
operator|.
name|Inject
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
name|FieldMapper
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
name|mapper
operator|.
name|core
operator|.
name|NumberFieldMapper
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
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|NumericRangeFilterParser
specifier|public
class|class
name|NumericRangeFilterParser
implements|implements
name|FilterParser
block|{
DECL|field|NAME
specifier|public
specifier|static
specifier|final
name|String
name|NAME
init|=
literal|"numeric_range"
decl_stmt|;
DECL|method|NumericRangeFilterParser
annotation|@
name|Inject
specifier|public
name|NumericRangeFilterParser
parameter_list|()
block|{     }
DECL|method|names
annotation|@
name|Override
specifier|public
name|String
index|[]
name|names
parameter_list|()
block|{
return|return
operator|new
name|String
index|[]
block|{
name|NAME
block|,
literal|"numericRange"
block|}
return|;
block|}
DECL|method|parse
annotation|@
name|Override
specifier|public
name|Filter
name|parse
parameter_list|(
name|QueryParseContext
name|parseContext
parameter_list|)
throws|throws
name|IOException
throws|,
name|QueryParsingException
block|{
name|XContentParser
name|parser
init|=
name|parseContext
operator|.
name|parser
argument_list|()
decl_stmt|;
name|boolean
name|cache
init|=
literal|false
decl_stmt|;
comment|// default to false, since its using fielddata cache
name|String
name|fieldName
init|=
literal|null
decl_stmt|;
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
name|filterName
init|=
literal|null
decl_stmt|;
name|String
name|currentFieldName
init|=
literal|null
decl_stmt|;
name|XContentParser
operator|.
name|Token
name|token
decl_stmt|;
while|while
condition|(
operator|(
name|token
operator|=
name|parser
operator|.
name|nextToken
argument_list|()
operator|)
operator|!=
name|XContentParser
operator|.
name|Token
operator|.
name|END_OBJECT
condition|)
block|{
if|if
condition|(
name|token
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|FIELD_NAME
condition|)
block|{
name|currentFieldName
operator|=
name|parser
operator|.
name|currentName
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|token
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|START_OBJECT
condition|)
block|{
name|fieldName
operator|=
name|currentFieldName
expr_stmt|;
while|while
condition|(
operator|(
name|token
operator|=
name|parser
operator|.
name|nextToken
argument_list|()
operator|)
operator|!=
name|XContentParser
operator|.
name|Token
operator|.
name|END_OBJECT
condition|)
block|{
if|if
condition|(
name|token
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|FIELD_NAME
condition|)
block|{
name|currentFieldName
operator|=
name|parser
operator|.
name|currentName
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
name|from
operator|=
name|parser
operator|.
name|textOrNull
argument_list|()
expr_stmt|;
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
name|to
operator|=
name|parser
operator|.
name|textOrNull
argument_list|()
expr_stmt|;
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
operator|||
literal|"includeLower"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|includeLower
operator|=
name|parser
operator|.
name|booleanValue
argument_list|()
expr_stmt|;
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
operator|||
literal|"includeUpper"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|includeUpper
operator|=
name|parser
operator|.
name|booleanValue
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"gt"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|from
operator|=
name|parser
operator|.
name|textOrNull
argument_list|()
expr_stmt|;
name|includeLower
operator|=
literal|false
expr_stmt|;
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
operator|||
literal|"ge"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|from
operator|=
name|parser
operator|.
name|textOrNull
argument_list|()
expr_stmt|;
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
name|to
operator|=
name|parser
operator|.
name|textOrNull
argument_list|()
expr_stmt|;
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
operator|||
literal|"le"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|to
operator|=
name|parser
operator|.
name|textOrNull
argument_list|()
expr_stmt|;
name|includeUpper
operator|=
literal|true
expr_stmt|;
block|}
block|}
block|}
block|}
elseif|else
if|if
condition|(
name|token
operator|.
name|isValue
argument_list|()
condition|)
block|{
if|if
condition|(
literal|"_name"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|filterName
operator|=
name|parser
operator|.
name|text
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"_cache"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|cache
operator|=
name|parser
operator|.
name|booleanValue
argument_list|()
expr_stmt|;
block|}
block|}
block|}
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
operator|==
literal|null
operator|||
operator|!
name|smartNameFieldMappers
operator|.
name|hasMapper
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|QueryParsingException
argument_list|(
name|parseContext
operator|.
name|index
argument_list|()
argument_list|,
literal|"failed to find mapping for field ["
operator|+
name|fieldName
operator|+
literal|"]"
argument_list|)
throw|;
block|}
name|FieldMapper
name|mapper
init|=
name|smartNameFieldMappers
operator|.
name|mapper
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
operator|(
name|mapper
operator|instanceof
name|NumberFieldMapper
operator|)
condition|)
block|{
throw|throw
operator|new
name|QueryParsingException
argument_list|(
name|parseContext
operator|.
name|index
argument_list|()
argument_list|,
literal|"Field ["
operator|+
name|fieldName
operator|+
literal|"] is not a numeric type"
argument_list|)
throw|;
block|}
name|Filter
name|filter
init|=
operator|(
operator|(
name|NumberFieldMapper
operator|)
name|mapper
operator|)
operator|.
name|rangeFilter
argument_list|(
name|parseContext
operator|.
name|indexCache
argument_list|()
operator|.
name|fieldData
argument_list|()
argument_list|,
name|from
argument_list|,
name|to
argument_list|,
name|includeLower
argument_list|,
name|includeUpper
argument_list|)
decl_stmt|;
if|if
condition|(
name|cache
condition|)
block|{
name|filter
operator|=
name|parseContext
operator|.
name|cacheFilter
argument_list|(
name|filter
argument_list|)
expr_stmt|;
block|}
name|filter
operator|=
name|wrapSmartNameFilter
argument_list|(
name|filter
argument_list|,
name|smartNameFieldMappers
argument_list|,
name|parseContext
argument_list|)
expr_stmt|;
if|if
condition|(
name|filterName
operator|!=
literal|null
condition|)
block|{
name|parseContext
operator|.
name|addNamedFilter
argument_list|(
name|filterName
argument_list|,
name|filter
argument_list|)
expr_stmt|;
block|}
return|return
name|filter
return|;
block|}
block|}
end_class

end_unit

