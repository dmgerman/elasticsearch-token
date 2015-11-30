begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.ingest.processor.gsub
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|ingest
operator|.
name|processor
operator|.
name|gsub
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ingest
operator|.
name|IngestDocument
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ingest
operator|.
name|processor
operator|.
name|ConfigurationUtils
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ingest
operator|.
name|processor
operator|.
name|Processor
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
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
import|import
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Matcher
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Pattern
import|;
end_import

begin_comment
comment|/**  * Processor that allows to search for patterns in field content and replace them with corresponding string replacement.  * Support fields of string type only, throws exception if a field is of a different type.  */
end_comment

begin_class
DECL|class|GsubProcessor
specifier|public
class|class
name|GsubProcessor
implements|implements
name|Processor
block|{
DECL|field|TYPE
specifier|public
specifier|static
specifier|final
name|String
name|TYPE
init|=
literal|"gsub"
decl_stmt|;
DECL|field|gsubExpressions
specifier|private
specifier|final
name|List
argument_list|<
name|GsubExpression
argument_list|>
name|gsubExpressions
decl_stmt|;
DECL|method|GsubProcessor
name|GsubProcessor
parameter_list|(
name|List
argument_list|<
name|GsubExpression
argument_list|>
name|gsubExpressions
parameter_list|)
block|{
name|this
operator|.
name|gsubExpressions
operator|=
name|gsubExpressions
expr_stmt|;
block|}
DECL|method|getGsubExpressions
name|List
argument_list|<
name|GsubExpression
argument_list|>
name|getGsubExpressions
parameter_list|()
block|{
return|return
name|gsubExpressions
return|;
block|}
annotation|@
name|Override
DECL|method|execute
specifier|public
name|void
name|execute
parameter_list|(
name|IngestDocument
name|document
parameter_list|)
block|{
for|for
control|(
name|GsubExpression
name|gsubExpression
range|:
name|gsubExpressions
control|)
block|{
name|String
name|oldVal
init|=
name|document
operator|.
name|getFieldValue
argument_list|(
name|gsubExpression
operator|.
name|getFieldName
argument_list|()
argument_list|,
name|String
operator|.
name|class
argument_list|)
decl_stmt|;
if|if
condition|(
name|oldVal
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"field ["
operator|+
name|gsubExpression
operator|.
name|getFieldName
argument_list|()
operator|+
literal|"] is null, cannot match pattern."
argument_list|)
throw|;
block|}
name|Matcher
name|matcher
init|=
name|gsubExpression
operator|.
name|getPattern
argument_list|()
operator|.
name|matcher
argument_list|(
name|oldVal
argument_list|)
decl_stmt|;
name|String
name|newVal
init|=
name|matcher
operator|.
name|replaceAll
argument_list|(
name|gsubExpression
operator|.
name|getReplacement
argument_list|()
argument_list|)
decl_stmt|;
name|document
operator|.
name|setFieldValue
argument_list|(
name|gsubExpression
operator|.
name|getFieldName
argument_list|()
argument_list|,
name|newVal
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|getType
specifier|public
name|String
name|getType
parameter_list|()
block|{
return|return
name|TYPE
return|;
block|}
DECL|class|Factory
specifier|public
specifier|static
class|class
name|Factory
implements|implements
name|Processor
operator|.
name|Factory
argument_list|<
name|GsubProcessor
argument_list|>
block|{
annotation|@
name|Override
DECL|method|create
specifier|public
name|GsubProcessor
name|create
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|config
parameter_list|)
throws|throws
name|Exception
block|{
name|List
argument_list|<
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|>
name|gsubConfig
init|=
name|ConfigurationUtils
operator|.
name|readList
argument_list|(
name|config
argument_list|,
literal|"expressions"
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|GsubExpression
argument_list|>
name|gsubExpressions
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|stringObjectMap
range|:
name|gsubConfig
control|)
block|{
name|String
name|field
init|=
name|stringObjectMap
operator|.
name|get
argument_list|(
literal|"field"
argument_list|)
decl_stmt|;
if|if
condition|(
name|field
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"no [field] specified for gsub expression"
argument_list|)
throw|;
block|}
name|String
name|pattern
init|=
name|stringObjectMap
operator|.
name|get
argument_list|(
literal|"pattern"
argument_list|)
decl_stmt|;
if|if
condition|(
name|pattern
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"no [pattern] specified for gsub expression"
argument_list|)
throw|;
block|}
name|String
name|replacement
init|=
name|stringObjectMap
operator|.
name|get
argument_list|(
literal|"replacement"
argument_list|)
decl_stmt|;
if|if
condition|(
name|replacement
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"no [replacement] specified for gsub expression"
argument_list|)
throw|;
block|}
name|Pattern
name|searchPattern
init|=
name|Pattern
operator|.
name|compile
argument_list|(
name|pattern
argument_list|)
decl_stmt|;
name|gsubExpressions
operator|.
name|add
argument_list|(
operator|new
name|GsubExpression
argument_list|(
name|field
argument_list|,
name|searchPattern
argument_list|,
name|replacement
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|GsubProcessor
argument_list|(
name|gsubExpressions
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

