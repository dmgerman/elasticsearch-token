begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|elasticsearch
operator|.
name|common
operator|.
name|xcontent
operator|.
name|XContentBuilder
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|script
operator|.
name|ScriptService
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
name|Map
import|;
end_import

begin_comment
comment|/**  * Facilitates creating template query requests.  * */
end_comment

begin_class
DECL|class|TemplateQueryBuilder
specifier|public
class|class
name|TemplateQueryBuilder
extends|extends
name|BaseQueryBuilder
block|{
comment|/** Parameters to fill the template with. */
DECL|field|vars
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|vars
decl_stmt|;
comment|/** Template to fill.*/
DECL|field|template
specifier|private
name|String
name|template
decl_stmt|;
DECL|field|templateType
specifier|private
name|ScriptService
operator|.
name|ScriptType
name|templateType
decl_stmt|;
comment|/**      * @param template the template to use for that query.      * @param vars the parameters to fill the template with.      * */
DECL|method|TemplateQueryBuilder
specifier|public
name|TemplateQueryBuilder
parameter_list|(
name|String
name|template
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|vars
parameter_list|)
block|{
name|this
argument_list|(
name|template
argument_list|,
name|ScriptService
operator|.
name|ScriptType
operator|.
name|INLINE
argument_list|,
name|vars
argument_list|)
expr_stmt|;
block|}
comment|/**      * @param template the template to use for that query.      * @param vars the parameters to fill the template with.      * @param templateType what kind of template (INLINE,FILE,ID)      * */
DECL|method|TemplateQueryBuilder
specifier|public
name|TemplateQueryBuilder
parameter_list|(
name|String
name|template
parameter_list|,
name|ScriptService
operator|.
name|ScriptType
name|templateType
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|vars
parameter_list|)
block|{
name|this
operator|.
name|template
operator|=
name|template
expr_stmt|;
name|this
operator|.
name|vars
operator|=
name|vars
expr_stmt|;
name|this
operator|.
name|templateType
operator|=
name|templateType
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|doXContent
specifier|protected
name|void
name|doXContent
parameter_list|(
name|XContentBuilder
name|builder
parameter_list|,
name|Params
name|params
parameter_list|)
throws|throws
name|IOException
block|{
name|builder
operator|.
name|startObject
argument_list|(
name|TemplateQueryParser
operator|.
name|NAME
argument_list|)
expr_stmt|;
name|String
name|fieldname
decl_stmt|;
switch|switch
condition|(
name|templateType
condition|)
block|{
case|case
name|FILE
case|:
name|fieldname
operator|=
literal|"file"
expr_stmt|;
break|break;
case|case
name|INDEXED
case|:
name|fieldname
operator|=
literal|"id"
expr_stmt|;
break|break;
case|case
name|INLINE
case|:
name|fieldname
operator|=
name|TemplateQueryParser
operator|.
name|QUERY
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Unknown template type "
operator|+
name|templateType
argument_list|)
throw|;
block|}
name|builder
operator|.
name|field
argument_list|(
name|fieldname
argument_list|,
name|template
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|TemplateQueryParser
operator|.
name|PARAMS
argument_list|,
name|vars
argument_list|)
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|parserName
specifier|protected
name|String
name|parserName
parameter_list|()
block|{
return|return
name|TemplateQueryParser
operator|.
name|NAME
return|;
block|}
block|}
end_class

end_unit

