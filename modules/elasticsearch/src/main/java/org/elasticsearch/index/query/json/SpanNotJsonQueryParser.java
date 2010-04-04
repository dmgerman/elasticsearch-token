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
name|Query
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
name|spans
operator|.
name|SpanNotQuery
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
name|spans
operator|.
name|SpanQuery
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

begin_comment
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|SpanNotJsonQueryParser
specifier|public
class|class
name|SpanNotJsonQueryParser
extends|extends
name|AbstractIndexComponent
implements|implements
name|JsonQueryParser
block|{
DECL|field|NAME
specifier|public
specifier|static
specifier|final
name|String
name|NAME
init|=
literal|"span_not"
decl_stmt|;
DECL|method|SpanNotJsonQueryParser
annotation|@
name|Inject
specifier|public
name|SpanNotJsonQueryParser
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
block|}
return|;
block|}
DECL|method|parse
annotation|@
name|Override
specifier|public
name|Query
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
name|float
name|boost
init|=
literal|1.0f
decl_stmt|;
name|SpanQuery
name|include
init|=
literal|null
decl_stmt|;
name|SpanQuery
name|exclude
init|=
literal|null
decl_stmt|;
name|String
name|currentFieldName
init|=
literal|null
decl_stmt|;
name|JsonToken
name|token
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
elseif|else
if|if
condition|(
name|token
operator|==
name|JsonToken
operator|.
name|START_OBJECT
condition|)
block|{
if|if
condition|(
literal|"include"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|Query
name|query
init|=
name|parseContext
operator|.
name|parseInnerQuery
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
operator|(
name|query
operator|instanceof
name|SpanQuery
operator|)
condition|)
block|{
throw|throw
operator|new
name|QueryParsingException
argument_list|(
name|index
argument_list|,
literal|"spanNot [include] must be of type span query"
argument_list|)
throw|;
block|}
name|include
operator|=
operator|(
name|SpanQuery
operator|)
name|query
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"exclude"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|Query
name|query
init|=
name|parseContext
operator|.
name|parseInnerQuery
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
operator|(
name|query
operator|instanceof
name|SpanQuery
operator|)
condition|)
block|{
throw|throw
operator|new
name|QueryParsingException
argument_list|(
name|index
argument_list|,
literal|"spanNot [exclude] must be of type span query"
argument_list|)
throw|;
block|}
name|exclude
operator|=
operator|(
name|SpanQuery
operator|)
name|query
expr_stmt|;
block|}
block|}
else|else
block|{
if|if
condition|(
literal|"boost"
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
name|VALUE_STRING
condition|)
block|{
name|boost
operator|=
name|Float
operator|.
name|parseFloat
argument_list|(
name|jp
operator|.
name|getText
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|boost
operator|=
name|jp
operator|.
name|getFloatValue
argument_list|()
expr_stmt|;
block|}
block|}
block|}
block|}
if|if
condition|(
name|include
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|QueryParsingException
argument_list|(
name|index
argument_list|,
literal|"spanNot must have [include] span query clause"
argument_list|)
throw|;
block|}
if|if
condition|(
name|exclude
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|QueryParsingException
argument_list|(
name|index
argument_list|,
literal|"spanNot must have [exclude] span query clause"
argument_list|)
throw|;
block|}
name|SpanNotQuery
name|query
init|=
operator|new
name|SpanNotQuery
argument_list|(
name|include
argument_list|,
name|exclude
argument_list|)
decl_stmt|;
name|query
operator|.
name|setBoost
argument_list|(
name|boost
argument_list|)
expr_stmt|;
return|return
name|query
return|;
block|}
block|}
end_class

end_unit

