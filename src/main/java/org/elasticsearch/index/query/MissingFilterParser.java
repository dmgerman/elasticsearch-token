begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|BooleanClause
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
name|lucene
operator|.
name|search
operator|.
name|NotFilter
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
name|search
operator|.
name|XBooleanFilter
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
name|MapperService
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
name|wrapSmartNameFilter
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|MissingFilterParser
specifier|public
class|class
name|MissingFilterParser
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
literal|"missing"
decl_stmt|;
annotation|@
name|Inject
DECL|method|MissingFilterParser
specifier|public
name|MissingFilterParser
parameter_list|()
block|{     }
annotation|@
name|Override
DECL|method|names
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
block|}
return|;
block|}
annotation|@
name|Override
DECL|method|parse
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
name|String
name|fieldName
init|=
literal|null
decl_stmt|;
name|String
name|filterName
init|=
literal|null
decl_stmt|;
name|boolean
name|nullValue
init|=
literal|false
decl_stmt|;
name|boolean
name|existence
init|=
literal|true
decl_stmt|;
name|XContentParser
operator|.
name|Token
name|token
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
operator|.
name|isValue
argument_list|()
condition|)
block|{
if|if
condition|(
literal|"field"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|fieldName
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
literal|"null_value"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|nullValue
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
literal|"existence"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|existence
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
else|else
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
literal|"[missing] filter does not support ["
operator|+
name|currentFieldName
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
block|}
if|if
condition|(
name|fieldName
operator|==
literal|null
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
literal|"missing must be provided with a [field]"
argument_list|)
throw|;
block|}
if|if
condition|(
operator|!
name|existence
operator|&&
operator|!
name|nullValue
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
literal|"missing must have either existence, or null_value, or both set to true"
argument_list|)
throw|;
block|}
name|Filter
name|existenceFilter
init|=
literal|null
decl_stmt|;
name|Filter
name|nullFilter
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
name|existence
condition|)
block|{
if|if
condition|(
name|smartNameFieldMappers
operator|!=
literal|null
operator|&&
name|smartNameFieldMappers
operator|.
name|hasMapper
argument_list|()
condition|)
block|{
name|existenceFilter
operator|=
name|smartNameFieldMappers
operator|.
name|mapper
argument_list|()
operator|.
name|rangeFilter
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|,
name|parseContext
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|existenceFilter
operator|==
literal|null
condition|)
block|{
name|existenceFilter
operator|=
operator|new
name|TermRangeFilter
argument_list|(
name|fieldName
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
comment|// we always cache this one, really does not change... (exists)
name|existenceFilter
operator|=
name|parseContext
operator|.
name|cacheFilter
argument_list|(
name|existenceFilter
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|existenceFilter
operator|=
operator|new
name|NotFilter
argument_list|(
name|existenceFilter
argument_list|)
expr_stmt|;
comment|// cache the not filter as well, so it will be faster
name|existenceFilter
operator|=
name|parseContext
operator|.
name|cacheFilter
argument_list|(
name|existenceFilter
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|nullValue
condition|)
block|{
if|if
condition|(
name|smartNameFieldMappers
operator|!=
literal|null
operator|&&
name|smartNameFieldMappers
operator|.
name|hasMapper
argument_list|()
condition|)
block|{
name|nullFilter
operator|=
name|smartNameFieldMappers
operator|.
name|mapper
argument_list|()
operator|.
name|nullValueFilter
argument_list|()
expr_stmt|;
if|if
condition|(
name|nullFilter
operator|!=
literal|null
condition|)
block|{
comment|// cache the not filter as well, so it will be faster
name|nullFilter
operator|=
name|parseContext
operator|.
name|cacheFilter
argument_list|(
name|nullFilter
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|Filter
name|filter
decl_stmt|;
if|if
condition|(
name|nullFilter
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|existenceFilter
operator|!=
literal|null
condition|)
block|{
name|XBooleanFilter
name|combined
init|=
operator|new
name|XBooleanFilter
argument_list|()
decl_stmt|;
name|combined
operator|.
name|add
argument_list|(
name|existenceFilter
argument_list|,
name|BooleanClause
operator|.
name|Occur
operator|.
name|SHOULD
argument_list|)
expr_stmt|;
name|combined
operator|.
name|add
argument_list|(
name|nullFilter
argument_list|,
name|BooleanClause
operator|.
name|Occur
operator|.
name|SHOULD
argument_list|)
expr_stmt|;
comment|// cache the not filter as well, so it will be faster
name|filter
operator|=
name|parseContext
operator|.
name|cacheFilter
argument_list|(
name|combined
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|filter
operator|=
name|nullFilter
expr_stmt|;
block|}
block|}
else|else
block|{
name|filter
operator|=
name|existenceFilter
expr_stmt|;
block|}
if|if
condition|(
name|filter
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
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
name|existenceFilter
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

