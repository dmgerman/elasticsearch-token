begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Copyright (C) 2006 Google Inc.  *  * Licensed under the Apache License, Version 2.0 (the "License");  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.inject.matcher
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|inject
operator|.
name|matcher
package|;
end_package

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|annotation
operator|.
name|Annotation
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|annotation
operator|.
name|Retention
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|annotation
operator|.
name|RetentionPolicy
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|AnnotatedElement
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|Method
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Objects
import|;
end_import

begin_comment
comment|/**  * Matcher implementations. Supports matching classes and methods.  *  * @author crazybob@google.com (Bob Lee)  */
end_comment

begin_class
DECL|class|Matchers
specifier|public
class|class
name|Matchers
block|{
DECL|method|Matchers
specifier|private
name|Matchers
parameter_list|()
block|{     }
comment|/**      * Returns a matcher which matches any input.      */
DECL|method|any
specifier|public
specifier|static
name|Matcher
argument_list|<
name|Object
argument_list|>
name|any
parameter_list|()
block|{
return|return
name|ANY
return|;
block|}
DECL|field|ANY
specifier|private
specifier|static
specifier|final
name|Matcher
argument_list|<
name|Object
argument_list|>
name|ANY
init|=
operator|new
name|Any
argument_list|()
decl_stmt|;
DECL|class|Any
specifier|private
specifier|static
class|class
name|Any
extends|extends
name|AbstractMatcher
argument_list|<
name|Object
argument_list|>
block|{
annotation|@
name|Override
DECL|method|matches
specifier|public
name|boolean
name|matches
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
return|return
literal|true
return|;
block|}
annotation|@
name|Override
DECL|method|toString
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"any()"
return|;
block|}
DECL|method|readResolve
specifier|public
name|Object
name|readResolve
parameter_list|()
block|{
return|return
name|any
argument_list|()
return|;
block|}
block|}
comment|/**      * Inverts the given matcher.      */
DECL|method|not
specifier|public
specifier|static
parameter_list|<
name|T
parameter_list|>
name|Matcher
argument_list|<
name|T
argument_list|>
name|not
parameter_list|(
specifier|final
name|Matcher
argument_list|<
name|?
super|super
name|T
argument_list|>
name|p
parameter_list|)
block|{
return|return
operator|new
name|Not
argument_list|<>
argument_list|(
name|p
argument_list|)
return|;
block|}
DECL|class|Not
specifier|private
specifier|static
class|class
name|Not
parameter_list|<
name|T
parameter_list|>
extends|extends
name|AbstractMatcher
argument_list|<
name|T
argument_list|>
block|{
DECL|field|delegate
specifier|final
name|Matcher
argument_list|<
name|?
super|super
name|T
argument_list|>
name|delegate
decl_stmt|;
DECL|method|Not
specifier|private
name|Not
parameter_list|(
name|Matcher
argument_list|<
name|?
super|super
name|T
argument_list|>
name|delegate
parameter_list|)
block|{
name|this
operator|.
name|delegate
operator|=
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|delegate
argument_list|,
literal|"delegate"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|matches
specifier|public
name|boolean
name|matches
parameter_list|(
name|T
name|t
parameter_list|)
block|{
return|return
operator|!
name|delegate
operator|.
name|matches
argument_list|(
name|t
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|equals
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|other
parameter_list|)
block|{
return|return
name|other
operator|instanceof
name|Not
operator|&&
operator|(
operator|(
name|Not
operator|)
name|other
operator|)
operator|.
name|delegate
operator|.
name|equals
argument_list|(
name|delegate
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|hashCode
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
operator|-
name|delegate
operator|.
name|hashCode
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|toString
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"not("
operator|+
name|delegate
operator|+
literal|")"
return|;
block|}
block|}
DECL|method|checkForRuntimeRetention
specifier|private
specifier|static
name|void
name|checkForRuntimeRetention
parameter_list|(
name|Class
argument_list|<
name|?
extends|extends
name|Annotation
argument_list|>
name|annotationType
parameter_list|)
block|{
name|Retention
name|retention
init|=
name|annotationType
operator|.
name|getAnnotation
argument_list|(
name|Retention
operator|.
name|class
argument_list|)
decl_stmt|;
if|if
condition|(
name|retention
operator|==
literal|null
operator|||
name|retention
operator|.
name|value
argument_list|()
operator|!=
name|RetentionPolicy
operator|.
name|RUNTIME
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Annotation "
operator|+
name|annotationType
operator|.
name|getSimpleName
argument_list|()
operator|+
literal|" is missing RUNTIME retention"
argument_list|)
throw|;
block|}
block|}
comment|/**      * Returns a matcher which matches elements (methods, classes, etc.)      * with a given annotation.      */
DECL|method|annotatedWith
specifier|public
specifier|static
name|Matcher
argument_list|<
name|AnnotatedElement
argument_list|>
name|annotatedWith
parameter_list|(
specifier|final
name|Class
argument_list|<
name|?
extends|extends
name|Annotation
argument_list|>
name|annotationType
parameter_list|)
block|{
return|return
operator|new
name|AnnotatedWithType
argument_list|(
name|annotationType
argument_list|)
return|;
block|}
DECL|class|AnnotatedWithType
specifier|private
specifier|static
class|class
name|AnnotatedWithType
extends|extends
name|AbstractMatcher
argument_list|<
name|AnnotatedElement
argument_list|>
block|{
DECL|field|annotationType
specifier|private
specifier|final
name|Class
argument_list|<
name|?
extends|extends
name|Annotation
argument_list|>
name|annotationType
decl_stmt|;
DECL|method|AnnotatedWithType
name|AnnotatedWithType
parameter_list|(
name|Class
argument_list|<
name|?
extends|extends
name|Annotation
argument_list|>
name|annotationType
parameter_list|)
block|{
name|this
operator|.
name|annotationType
operator|=
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|annotationType
argument_list|,
literal|"annotation type"
argument_list|)
expr_stmt|;
name|checkForRuntimeRetention
argument_list|(
name|annotationType
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|matches
specifier|public
name|boolean
name|matches
parameter_list|(
name|AnnotatedElement
name|element
parameter_list|)
block|{
return|return
name|element
operator|.
name|getAnnotation
argument_list|(
name|annotationType
argument_list|)
operator|!=
literal|null
return|;
block|}
annotation|@
name|Override
DECL|method|equals
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|other
parameter_list|)
block|{
return|return
name|other
operator|instanceof
name|AnnotatedWithType
operator|&&
operator|(
operator|(
name|AnnotatedWithType
operator|)
name|other
operator|)
operator|.
name|annotationType
operator|.
name|equals
argument_list|(
name|annotationType
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|hashCode
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
literal|37
operator|*
name|annotationType
operator|.
name|hashCode
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|toString
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"annotatedWith("
operator|+
name|annotationType
operator|.
name|getSimpleName
argument_list|()
operator|+
literal|".class)"
return|;
block|}
block|}
comment|/**      * Returns a matcher which matches elements (methods, classes, etc.)      * with a given annotation.      */
DECL|method|annotatedWith
specifier|public
specifier|static
name|Matcher
argument_list|<
name|AnnotatedElement
argument_list|>
name|annotatedWith
parameter_list|(
specifier|final
name|Annotation
name|annotation
parameter_list|)
block|{
return|return
operator|new
name|AnnotatedWith
argument_list|(
name|annotation
argument_list|)
return|;
block|}
DECL|class|AnnotatedWith
specifier|private
specifier|static
class|class
name|AnnotatedWith
extends|extends
name|AbstractMatcher
argument_list|<
name|AnnotatedElement
argument_list|>
block|{
DECL|field|annotation
specifier|private
specifier|final
name|Annotation
name|annotation
decl_stmt|;
DECL|method|AnnotatedWith
name|AnnotatedWith
parameter_list|(
name|Annotation
name|annotation
parameter_list|)
block|{
name|this
operator|.
name|annotation
operator|=
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|annotation
argument_list|,
literal|"annotation"
argument_list|)
expr_stmt|;
name|checkForRuntimeRetention
argument_list|(
name|annotation
operator|.
name|annotationType
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|matches
specifier|public
name|boolean
name|matches
parameter_list|(
name|AnnotatedElement
name|element
parameter_list|)
block|{
name|Annotation
name|fromElement
init|=
name|element
operator|.
name|getAnnotation
argument_list|(
name|annotation
operator|.
name|annotationType
argument_list|()
argument_list|)
decl_stmt|;
return|return
name|fromElement
operator|!=
literal|null
operator|&&
name|annotation
operator|.
name|equals
argument_list|(
name|fromElement
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|equals
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|other
parameter_list|)
block|{
return|return
name|other
operator|instanceof
name|AnnotatedWith
operator|&&
operator|(
operator|(
name|AnnotatedWith
operator|)
name|other
operator|)
operator|.
name|annotation
operator|.
name|equals
argument_list|(
name|annotation
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|hashCode
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
literal|37
operator|*
name|annotation
operator|.
name|hashCode
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|toString
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"annotatedWith("
operator|+
name|annotation
operator|+
literal|")"
return|;
block|}
block|}
comment|/**      * Returns a matcher which matches subclasses of the given type (as well as      * the given type).      */
DECL|method|subclassesOf
specifier|public
specifier|static
name|Matcher
argument_list|<
name|Class
argument_list|>
name|subclassesOf
parameter_list|(
specifier|final
name|Class
argument_list|<
name|?
argument_list|>
name|superclass
parameter_list|)
block|{
return|return
operator|new
name|SubclassesOf
argument_list|(
name|superclass
argument_list|)
return|;
block|}
DECL|class|SubclassesOf
specifier|private
specifier|static
class|class
name|SubclassesOf
extends|extends
name|AbstractMatcher
argument_list|<
name|Class
argument_list|>
block|{
DECL|field|superclass
specifier|private
specifier|final
name|Class
argument_list|<
name|?
argument_list|>
name|superclass
decl_stmt|;
DECL|method|SubclassesOf
name|SubclassesOf
parameter_list|(
name|Class
argument_list|<
name|?
argument_list|>
name|superclass
parameter_list|)
block|{
name|this
operator|.
name|superclass
operator|=
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|superclass
argument_list|,
literal|"superclass"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|matches
specifier|public
name|boolean
name|matches
parameter_list|(
name|Class
name|subclass
parameter_list|)
block|{
return|return
name|superclass
operator|.
name|isAssignableFrom
argument_list|(
name|subclass
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|equals
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|other
parameter_list|)
block|{
return|return
name|other
operator|instanceof
name|SubclassesOf
operator|&&
operator|(
operator|(
name|SubclassesOf
operator|)
name|other
operator|)
operator|.
name|superclass
operator|.
name|equals
argument_list|(
name|superclass
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|hashCode
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
literal|37
operator|*
name|superclass
operator|.
name|hashCode
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|toString
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"subclassesOf("
operator|+
name|superclass
operator|.
name|getSimpleName
argument_list|()
operator|+
literal|".class)"
return|;
block|}
block|}
comment|/**      * Returns a matcher which matches objects equal to the given object.      */
DECL|method|only
specifier|public
specifier|static
name|Matcher
argument_list|<
name|Object
argument_list|>
name|only
parameter_list|(
name|Object
name|value
parameter_list|)
block|{
return|return
operator|new
name|Only
argument_list|(
name|value
argument_list|)
return|;
block|}
DECL|class|Only
specifier|private
specifier|static
class|class
name|Only
extends|extends
name|AbstractMatcher
argument_list|<
name|Object
argument_list|>
block|{
DECL|field|value
specifier|private
specifier|final
name|Object
name|value
decl_stmt|;
DECL|method|Only
name|Only
parameter_list|(
name|Object
name|value
parameter_list|)
block|{
name|this
operator|.
name|value
operator|=
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|value
argument_list|,
literal|"value"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|matches
specifier|public
name|boolean
name|matches
parameter_list|(
name|Object
name|other
parameter_list|)
block|{
return|return
name|value
operator|.
name|equals
argument_list|(
name|other
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|equals
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|other
parameter_list|)
block|{
return|return
name|other
operator|instanceof
name|Only
operator|&&
operator|(
operator|(
name|Only
operator|)
name|other
operator|)
operator|.
name|value
operator|.
name|equals
argument_list|(
name|value
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|hashCode
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
literal|37
operator|*
name|value
operator|.
name|hashCode
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|toString
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"only("
operator|+
name|value
operator|+
literal|")"
return|;
block|}
block|}
comment|/**      * Returns a matcher which matches only the given object.      */
DECL|method|identicalTo
specifier|public
specifier|static
name|Matcher
argument_list|<
name|Object
argument_list|>
name|identicalTo
parameter_list|(
specifier|final
name|Object
name|value
parameter_list|)
block|{
return|return
operator|new
name|IdenticalTo
argument_list|(
name|value
argument_list|)
return|;
block|}
DECL|class|IdenticalTo
specifier|private
specifier|static
class|class
name|IdenticalTo
extends|extends
name|AbstractMatcher
argument_list|<
name|Object
argument_list|>
block|{
DECL|field|value
specifier|private
specifier|final
name|Object
name|value
decl_stmt|;
DECL|method|IdenticalTo
name|IdenticalTo
parameter_list|(
name|Object
name|value
parameter_list|)
block|{
name|this
operator|.
name|value
operator|=
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|value
argument_list|,
literal|"value"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|matches
specifier|public
name|boolean
name|matches
parameter_list|(
name|Object
name|other
parameter_list|)
block|{
return|return
name|value
operator|==
name|other
return|;
block|}
annotation|@
name|Override
DECL|method|equals
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|other
parameter_list|)
block|{
return|return
name|other
operator|instanceof
name|IdenticalTo
operator|&&
operator|(
operator|(
name|IdenticalTo
operator|)
name|other
operator|)
operator|.
name|value
operator|==
name|value
return|;
block|}
annotation|@
name|Override
DECL|method|hashCode
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
literal|37
operator|*
name|System
operator|.
name|identityHashCode
argument_list|(
name|value
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|toString
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"identicalTo("
operator|+
name|value
operator|+
literal|")"
return|;
block|}
block|}
comment|/**      * Returns a matcher which matches classes in the given package. Packages are specific to their      * classloader, so classes with the same package name may not have the same package at runtime.      */
DECL|method|inPackage
specifier|public
specifier|static
name|Matcher
argument_list|<
name|Class
argument_list|>
name|inPackage
parameter_list|(
specifier|final
name|Package
name|targetPackage
parameter_list|)
block|{
return|return
operator|new
name|InPackage
argument_list|(
name|targetPackage
argument_list|)
return|;
block|}
DECL|class|InPackage
specifier|private
specifier|static
class|class
name|InPackage
extends|extends
name|AbstractMatcher
argument_list|<
name|Class
argument_list|>
block|{
DECL|field|targetPackage
specifier|private
specifier|final
specifier|transient
name|Package
name|targetPackage
decl_stmt|;
DECL|field|packageName
specifier|private
specifier|final
name|String
name|packageName
decl_stmt|;
DECL|method|InPackage
name|InPackage
parameter_list|(
name|Package
name|targetPackage
parameter_list|)
block|{
name|this
operator|.
name|targetPackage
operator|=
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|targetPackage
argument_list|,
literal|"package"
argument_list|)
expr_stmt|;
name|this
operator|.
name|packageName
operator|=
name|targetPackage
operator|.
name|getName
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|matches
specifier|public
name|boolean
name|matches
parameter_list|(
name|Class
name|c
parameter_list|)
block|{
return|return
name|c
operator|.
name|getPackage
argument_list|()
operator|.
name|equals
argument_list|(
name|targetPackage
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|equals
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|other
parameter_list|)
block|{
return|return
name|other
operator|instanceof
name|InPackage
operator|&&
operator|(
operator|(
name|InPackage
operator|)
name|other
operator|)
operator|.
name|targetPackage
operator|.
name|equals
argument_list|(
name|targetPackage
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|hashCode
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
literal|37
operator|*
name|targetPackage
operator|.
name|hashCode
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|toString
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"inPackage("
operator|+
name|targetPackage
operator|.
name|getName
argument_list|()
operator|+
literal|")"
return|;
block|}
DECL|method|readResolve
specifier|public
name|Object
name|readResolve
parameter_list|()
block|{
return|return
name|inPackage
argument_list|(
name|Package
operator|.
name|getPackage
argument_list|(
name|packageName
argument_list|)
argument_list|)
return|;
block|}
block|}
comment|/**      * Returns a matcher which matches classes in the given package and its subpackages. Unlike      * {@link #inPackage(Package) inPackage()}, this matches classes from any classloader.      *      * @since 2.0      */
DECL|method|inSubpackage
specifier|public
specifier|static
name|Matcher
argument_list|<
name|Class
argument_list|>
name|inSubpackage
parameter_list|(
specifier|final
name|String
name|targetPackageName
parameter_list|)
block|{
return|return
operator|new
name|InSubpackage
argument_list|(
name|targetPackageName
argument_list|)
return|;
block|}
DECL|class|InSubpackage
specifier|private
specifier|static
class|class
name|InSubpackage
extends|extends
name|AbstractMatcher
argument_list|<
name|Class
argument_list|>
block|{
DECL|field|targetPackageName
specifier|private
specifier|final
name|String
name|targetPackageName
decl_stmt|;
DECL|method|InSubpackage
name|InSubpackage
parameter_list|(
name|String
name|targetPackageName
parameter_list|)
block|{
name|this
operator|.
name|targetPackageName
operator|=
name|targetPackageName
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|matches
specifier|public
name|boolean
name|matches
parameter_list|(
name|Class
name|c
parameter_list|)
block|{
name|String
name|classPackageName
init|=
name|c
operator|.
name|getPackage
argument_list|()
operator|.
name|getName
argument_list|()
decl_stmt|;
return|return
name|classPackageName
operator|.
name|equals
argument_list|(
name|targetPackageName
argument_list|)
operator|||
name|classPackageName
operator|.
name|startsWith
argument_list|(
name|targetPackageName
operator|+
literal|"."
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|equals
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|other
parameter_list|)
block|{
return|return
name|other
operator|instanceof
name|InSubpackage
operator|&&
operator|(
operator|(
name|InSubpackage
operator|)
name|other
operator|)
operator|.
name|targetPackageName
operator|.
name|equals
argument_list|(
name|targetPackageName
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|hashCode
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
literal|37
operator|*
name|targetPackageName
operator|.
name|hashCode
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|toString
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"inSubpackage("
operator|+
name|targetPackageName
operator|+
literal|")"
return|;
block|}
block|}
comment|/**      * Returns a matcher which matches methods with matching return types.      */
DECL|method|returns
specifier|public
specifier|static
name|Matcher
argument_list|<
name|Method
argument_list|>
name|returns
parameter_list|(
specifier|final
name|Matcher
argument_list|<
name|?
super|super
name|Class
argument_list|<
name|?
argument_list|>
argument_list|>
name|returnType
parameter_list|)
block|{
return|return
operator|new
name|Returns
argument_list|(
name|returnType
argument_list|)
return|;
block|}
DECL|class|Returns
specifier|private
specifier|static
class|class
name|Returns
extends|extends
name|AbstractMatcher
argument_list|<
name|Method
argument_list|>
block|{
DECL|field|returnType
specifier|private
specifier|final
name|Matcher
argument_list|<
name|?
super|super
name|Class
argument_list|<
name|?
argument_list|>
argument_list|>
name|returnType
decl_stmt|;
DECL|method|Returns
name|Returns
parameter_list|(
name|Matcher
argument_list|<
name|?
super|super
name|Class
argument_list|<
name|?
argument_list|>
argument_list|>
name|returnType
parameter_list|)
block|{
name|this
operator|.
name|returnType
operator|=
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|returnType
argument_list|,
literal|"return type matcher"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|matches
specifier|public
name|boolean
name|matches
parameter_list|(
name|Method
name|m
parameter_list|)
block|{
return|return
name|returnType
operator|.
name|matches
argument_list|(
name|m
operator|.
name|getReturnType
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|equals
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|other
parameter_list|)
block|{
return|return
name|other
operator|instanceof
name|Returns
operator|&&
operator|(
operator|(
name|Returns
operator|)
name|other
operator|)
operator|.
name|returnType
operator|.
name|equals
argument_list|(
name|returnType
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|hashCode
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
literal|37
operator|*
name|returnType
operator|.
name|hashCode
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|toString
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"returns("
operator|+
name|returnType
operator|+
literal|")"
return|;
block|}
block|}
block|}
end_class

end_unit

