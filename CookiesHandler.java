
home | career | drupal | java | mac | mysql | perl | scala | uml | unix	 

 

 	
What this is
This file is included in the DevDaily.com "Java Source Code Warehouse" project. The intent of this project is to help you "Learn Java by Example" TM.

Other links
 The search page
 Other source code files at this package level
 Click here to learn more about this project
The source code
/*
 * $Header: /home/cvs/jakarta-commons/httpclient/src/java/org/apache/commons/httpclient/Cookie.java,v 1.38.2.4 2004/06/05 16:32:01 olegk Exp $
 * $Revision: 1.38.2.4 $
 * $Date: 2004/06/05 16:32:01 $
 *
 * ====================================================================
 *
 *  Copyright 1999-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * .
 *
 * [Additional notices, if required by prior licensing conditions]
 *
 */

package org.apache.commons.httpclient;

import java.io.Serializable;
import java.text.RuleBasedCollator;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.cookie.CookieSpec;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * 

 * HTTP "magic-cookie" represents a piece of state information
 * that the HTTP agent and the target server can exchange to maintain 
 * a session.
 * 


 * 
 * @author B.C. Holmes
 * @author Park, Sung-Gu
 * @author Doug Sale
 * @author Rod Waldhoff
 * @author dIon Gillard
 * @author Sean C. Sullivan
 * @author John Evans
 * @author Marc A. Saegesser
 * @author Oleg Kalnichevski
 * @author Mike Bowler
 * 
 * @version $Revision: 1.38.2.4 $ $Date: 2004/06/05 16:32:01 $
 */

public class Cookie extends NameValuePair implements Serializable, Comparator {

    // ----------------------------------------------------------- Constructors

    /**
     * Default constructor. Creates a blank cookie 
     */

    public Cookie() {
        this(null, "noname", null, null, null, false);
    }

    /**
     * Creates a cookie with the given name, value and domain attribute.
     *
     * @param name    the cookie name
     * @param value   the cookie value
     * @param domain  the domain this cookie can be sent to
     */
    public Cookie(String domain, String name, String value) {
        this(domain, name, value, null, null, false);
    }

    /**
     * Creates a cookie with the given name, value, domain attribute,
     * path attribute, expiration attribute, and secure attribute 
     *
     * @param name    the cookie name
     * @param value   the cookie value
     * @param domain  the domain this cookie can be sent to
     * @param path    the path prefix for which this cookie can be sent
     * @param expires the {@link Date} at which this cookie expires,
     *                or null if the cookie expires at the end
     *                of the session
     * @param secure if true this cookie can only be sent over secure
     * connections
     * @throws IllegalArgumentException If cookie name is null or blank,
     *   cookie name contains a blank, or cookie name starts with character $
     *   
     */
    public Cookie(String domain, String name, String value, 
        String path, Date expires, boolean secure) {
            
        super(name, value);
        LOG.trace("enter Cookie(String, String, String, String, Date, boolean)");
        if (name == null) {
            throw new IllegalArgumentException("Cookie name may not be null");
        }
        if (name.trim().equals("")) {
            throw new IllegalArgumentException("Cookie name may not be blank");
        }
        this.setPath(path);
        this.setDomain(domain);
        this.setExpiryDate(expires);
        this.setSecure(secure);
    }

    /**
     * Creates a cookie with the given name, value, domain attribute,
     * path attribute, maximum age attribute, and secure attribute 
     *
     * @param name   the cookie name
     * @param value  the cookie value
     * @param domain the domain this cookie can be sent to
     * @param path   the path prefix for which this cookie can be sent
     * @param maxAge the number of seconds for which this cookie is valid.
     *               maxAge is expected to be a non-negative number. 
     *               -1 signifies that the cookie should never expire.
     * @param secure if true this cookie can only be sent over secure
     * connections
     */
    public Cookie(String domain, String name, String value, String path, 
        int maxAge, boolean secure) {
            
        this(domain, name, value, path, null, secure);
        if (maxAge < -1) {
            throw new IllegalArgumentException("Invalid max age:  " + Integer.toString(maxAge));
        }            
        if (maxAge >= 0) {
            setExpiryDate(new Date(System.currentTimeMillis() + maxAge * 1000L));
        }
    }

    /**
     * Returns the comment describing the purpose of this cookie, or
     * null if no such comment has been defined.
     * 
     * @return comment 
     *
     * @see #setComment(String)
     */
    public String getComment() {
        return cookieComment;
    }

    /**
     * If a user agent (web browser) presents this cookie to a user, the
     * cookie's purpose will be described using this comment.
     * 
     * @param comment
     *  
     * @see #getComment()
     */
    public void setComment(String comment) {
        cookieComment = comment;
    }

    /**
     * Returns the expiration {@link Date} of the cookie, or null
     * if none exists.
     * 
Note: the object returned by this method is 
     * considered immutable. Changing it (e.g. using setTime()) could result
     * in undefined behaviour. Do so at your peril. 


     * @return Expiration {@link Date}, or null.
     *
     * @see #setExpiryDate(java.util.Date)
     *
     */
    public Date getExpiryDate() {
        return cookieExpiryDate;
    }

    /**
     * Sets expiration date.
     * 
Note: the object returned by this method is considered
     * immutable. Changing it (e.g. using setTime()) could result in undefined 
     * behaviour. Do so at your peril.


     *
     * @param expiryDate the {@link Date} after which this cookie is no longer valid.
     *
     * @see #getExpiryDate
     *
     */
    public void setExpiryDate (Date expiryDate) {
        cookieExpiryDate = expiryDate;
    }


    /**
     * Returns false if the cookie should be discarded at the end
     * of the "session"; true otherwise.
     *
     * @return false if the cookie should be discarded at the end
     *         of the "session"; true otherwise
     */
    public boolean isPersistent() {
        return (null != cookieExpiryDate);
    }


    /**
     * Returns domain attribute of the cookie.
     * 
     * @return the value of the domain attribute
     *
     * @see #setDomain(java.lang.String)
     */
    public String getDomain() {
        return cookieDomain;
    }

    /**
     * Sets the domain attribute.
     * 
     * @param domain The value of the domain attribute
     *
     * @see #getDomain
     */
    public void setDomain(String domain) {
        if (domain != null) {
            int ndx = domain.indexOf(":");
            if (ndx != -1) {
              domain = domain.substring(0, ndx);
            }
            cookieDomain = domain.toLowerCase();
        }
    }


    /**
     * Returns the path attribute of the cookie
     * 
     * @return The value of the path attribute.
     * 
     * @see #setPath(java.lang.String)
     */
    public String getPath() {
        return cookiePath;
    }

    /**
     * Sets the path attribute.
     *
     * @param path The value of the path attribute
     *
     * @see #getPath
     *
     */
    public void setPath(String path) {
        cookiePath = path;
    }

    /**
     * @return true if this cookie should only be sent over secure connections.
     * @see #setSecure(boolean)
     */
    public boolean getSecure() {
        return isSecure;
    }

    /**
     * Sets the secure attribute of the cookie.
     * 

     * When true the cookie should only be sent
     * using a secure protocol (https).  This should only be set when
     * the cookie's originating server used a secure protocol to set the
     * cookie's value.
     *
     * @param secure The value of the secure attribute
     * 
     * @see #getSecure()
     */
    public void setSecure (boolean secure) {
        isSecure = secure;
    }

    /**
     * Returns the version of the cookie specification to which this
     * cookie conforms.
     *
     * @return the version of the cookie.
     * 
     * @see #setVersion(int)
     *
     */
    public int getVersion() {
        return cookieVersion;
    }

    /**
     * Sets the version of the cookie specification to which this
     * cookie conforms. 
     *
     * @param version the version of the cookie.
     * 
     * @see #getVersion
     */
    public void setVersion(int version) {
        cookieVersion = version;
    }

    /**
     * Returns true if this cookie has expired.
     * 
     * @return true if the cookie has expired.
     */
    public boolean isExpired() {
        return (cookieExpiryDate != null  
            && cookieExpiryDate.getTime() <= System.currentTimeMillis());
    }

    /**
     * Returns true if this cookie has expired according to the time passed in.
     * 
     * @param now The current time.
     * 
     * @return true if the cookie expired.
     */
    public boolean isExpired(Date now) {
        return (cookieExpiryDate != null  
            && cookieExpiryDate.getTime() <= now.getTime());
    }


    /**
     * Indicates whether the cookie had a path specified in a 
     * path attribute of the Set-Cookie header. This value
     * is important for generating the Cookie header because 
     * some cookie specifications require that the Cookie header 
     * should only include a path attribute if the cookie's path 
     * was specified in the Set-Cookie header.
     *
     * @param value true if the cookie's path was explicitly 
     * set, false otherwise.
     * 
     * @see #isPathAttributeSpecified
     */
    public void setPathAttributeSpecified(boolean value) {
        hasPathAttribute = value;
    }

    /**
     * Returns true if cookie's path was set via a path attribute
     * in the Set-Cookie header.
     *
     * @return value true if the cookie's path was explicitly 
     * set, false otherwise.
     * 
     * @see #setPathAttributeSpecified
     */
    public boolean isPathAttributeSpecified() {
        return hasPathAttribute;
    }

    /**
     * Indicates whether the cookie had a domain specified in a 
     * domain attribute of the Set-Cookie header. This value
     * is important for generating the Cookie header because 
     * some cookie specifications require that the Cookie header 
     * should only include a domain attribute if the cookie's domain 
     * was specified in the Set-Cookie header.
     *
     * @param value true if the cookie's domain was explicitly 
     * set, false otherwise.
     *
     * @see #isDomainAttributeSpecified
     */
    public void setDomainAttributeSpecified(boolean value) {
        hasDomainAttribute = value;
    }

    /**
     * Returns true if cookie's domain was set via a domain 
     * attribute in the Set-Cookie header.
     *
     * @return value true if the cookie's domain was explicitly 
     * set, false otherwise.
     *
     * @see #setDomainAttributeSpecified
     */
    public boolean isDomainAttributeSpecified() {
        return hasDomainAttribute;
    }

    /**
     * Returns a hash code in keeping with the
     * {@link Object#hashCode} general hashCode contract.
     * @return A hash code
     */
    public int hashCode() {
        return super.hashCode()
            ^ (null == cookiePath ? 0 : cookiePath.hashCode())
            ^ (null == cookieDomain ? 0 : cookieDomain.hashCode());
    }


    /**
     * Two cookies are equal if the name, path and domain match.
     * @param obj The object to compare against.
     * @return true if the two objects are equal.
     */
    public boolean equals(Object obj) {
        LOG.trace("enter Cookie.equals(Object)");
        
        if ((obj != null) && (obj instanceof Cookie)) {
            Cookie that = (Cookie) obj;
            return 
                (null == this.getName() 
                    ? null == that.getName() 
                    : this.getName().equals(that.getName())) 
                && (null == this.getPath() 
                    ? null == that.getPath() 
                    : this.getPath().equals(that.getPath())) 
                && (null == this.getDomain() 
                    ? null == that.getDomain() 
                    : this.getDomain().equals(that.getDomain()));
        } else {
            return false;
        }
    }


    /**
     * Returns a textual representation of the cookie.
     * 
     * @return string .
     */
    public String toExternalForm() {
        return CookiePolicy.getSpecByVersion(
            getVersion()).formatCookie(this);
    }

    /**
     * Return true if I should be submitted with a request with given
     * attributes, false otherwise.
     * @param domain the host to which the request is being submitted
     * @param port the port to which the request is being submitted (currently
     * ignored)
     * @param path the path to which the request is being submitted
     * @param secure true if the request is using the HTTPS protocol
     * @param date the time at which the request is submitted
     * @return true if the cookie matches
     * 
     * @deprecated use {@link CookieSpec} interface
     */
    public boolean matches(
        String domain, int port, String path, boolean secure, Date date) {
            
        LOG.trace("enter Cookie.matches(Strinng, int, String, boolean, Date");
        CookieSpec matcher = CookiePolicy.getDefaultSpec();
        return matcher.match(domain, port, path, secure, this);
    }

    /**
     * Return true if I should be submitted with a request with given
     * attributes, false otherwise.
     * @param domain the host to which the request is being submitted
     * @param port the port to which the request is being submitted (currently
     * ignored)
     * @param path the path to which the request is being submitted
     * @param secure True if this cookie has the secure flag set
     * @return true if I should be submitted as above.
     * @deprecated use {@link CookieSpec} interface
     */
    public boolean matches(
        String domain, int port, String path, boolean secure) {
        LOG.trace("enter Cookie.matches(String, int, String, boolean");
        return matches(domain, port, path, secure, new Date());
    }

    /**
     * Create a Cookie header containing
     * all non-expired cookies in cookies,
     * associated with the given domain and
     * path, assuming the connection is not
     * secure.
     * 


     * If no cookies match, returns null.
     * 
     * @param domain The domain
     * @param path The path
     * @param cookies The cookies to use
     * @return The new header.
     * @deprecated use {@link CookieSpec} interface
     */
    public static Header createCookieHeader(String domain, String path, 
        Cookie[] cookies) {
            
        LOG.trace("enter Cookie.createCookieHeader(String,String,Cookie[])");
        return Cookie.createCookieHeader(domain, path, false, cookies);
    }

    /**
     * Create a Cookie header containing
     * all non-expired cookies in cookies,
     * associated with the given domain, path and
     * https setting.
     * 


     * If no cookies match, returns null.
     * 
     * @param domain The domain
     * @param path The path
     * @param secure True if this cookie has the secure flag set
     * @param cookies The cookies to use.
     * @return The new header
     * @exception IllegalArgumentException if domain or path is null
     * 
     * @deprecated use {@link CookieSpec} interface
     */
    public static Header createCookieHeader(String domain, String path, 
        boolean secure, Cookie[] cookies)
        throws IllegalArgumentException {
            
        LOG.trace("enter Cookie.createCookieHeader("
            + "String, String, boolean, Cookie[])");

        // Make sure domain isn't null here.  Path will be validated in 
        // subsequent call to createCookieHeader
        if (domain == null) {
            throw new IllegalArgumentException("null domain in "
                + "createCookieHeader.");
        }
        // parse port from domain, if any
        int port = secure ? 443 : 80;
        int ndx = domain.indexOf(":");
        if (ndx != -1) {
            try {
                port = Integer.parseInt(domain.substring(ndx + 1, 
                    domain.length()));
            } catch (NumberFormatException e) {
                // ignore?, but at least LOG
                LOG.warn("Cookie.createCookieHeader():  "
                    + "Invalid port number in domain " + domain);
            }
        }
        return Cookie.createCookieHeader(domain, port, path, secure, cookies);
    }

    /**
     * Create a Cookie header containing
     * all non-expired cookies in cookies,
     * associated with the given domain, port,
     * path and https setting.
     * 


     * If no cookies match, returns null.
     * 
     * @param domain The domain
     * @param port The port
     * @param path The path
     * @param secure True if this cookie has the secure flag set
     * @param cookies The cookies to use.
     * @return The new header
     * @throws IllegalArgumentException if domain or path is null
     * 
     * @deprecated use {@link CookieSpec} interface
     */
    public static Header createCookieHeader(String domain, int port, 
        String path, boolean secure, Cookie[] cookies) 
        throws IllegalArgumentException {
        LOG.trace("enter Cookie.createCookieHeader(String, int, String, boolean, Cookie[])");
        return Cookie.createCookieHeader(domain, port, path, secure, new Date(), cookies);
    }

    /**
     * Create a Cookie header containing all cookies in cookies,
     * associated with the given domain, port, path and
     * https setting, and which are not expired according to the given
     * date.
     * 


     * If no cookies match, returns null.
     * 
     * @param domain The domain
     * @param port The port
     * @param path The path
     * @param secure True if this cookie has the secure flag set
     * @param now The date to check for expiry
     * @param cookies The cookies to use.
     * @return The new header
     * @throws IllegalArgumentException if domain or path is null
     * 
     * @deprecated use {@link CookieSpec} interface
     */

    public static Header createCookieHeader(
        String domain, int port, String path, boolean secure, 
        Date now, Cookie[] cookies) 
        throws IllegalArgumentException {
            
        LOG.trace("enter Cookie.createCookieHeader(String, int, String, boolean, Date, Cookie[])");
        CookieSpec matcher = CookiePolicy.getDefaultSpec();
        cookies = matcher.match(domain, port, path, secure, cookies);
        if ((cookies != null) && (cookies.length > 0)) {
            return matcher.formatCookieHeader(cookies);
        } else {
            return null;
        } 
    }

    /**
     * 

Compares two cookies to determine order for cookie header.


     * 
Most specific should be first. 


     * 
This method is implemented so a cookie can be used as a comparator for
     * a SortedSet of cookies. Specifically it's used above in the 
     * createCookieHeader method.


     * @param o1 The first object to be compared
     * @param o2 The second object to be compared
     * @return See {@link java.util.Comparator#compare(Object,Object)}
     */
    public int compare(Object o1, Object o2) {
        LOG.trace("enter Cookie.compare(Object, Object)");

        if (!(o1 instanceof Cookie)) {
            throw new ClassCastException(o1.getClass().getName());
        }
        if (!(o2 instanceof Cookie)) {
            throw new ClassCastException(o2.getClass().getName());
        }
        Cookie c1 = (Cookie) o1;
        Cookie c2 = (Cookie) o2;
        if (c1.getPath() == null && c2.getPath() == null) {
            return 0;
        } else if (c1.getPath() == null) {
            // null is assumed to be "/"
            if (c2.getPath().equals(CookieSpec.PATH_DELIM)) {
                return 0;
            } else {
                return -1;
            }
        } else if (c2.getPath() == null) {
            // null is assumed to be "/"
            if (c1.getPath().equals(CookieSpec.PATH_DELIM)) {
                return 0;
            } else {
                return 1;
            }
        } else {
            return STRING_COLLATOR.compare(c1.getPath(), c2.getPath());
        }
    }

    /**
     * Return a textual representation of the cookie.
     * @see #toExternalForm
     */
    public String toString() {
        return toExternalForm();
    }

    /**
     * Parses the Set-Cookie {@link Header} into an array of
     * Cookies, assuming that the cookies were recieved
     * on an insecure channel.
     *
     * @param domain the domain from which the {@link Header} was received
     * @param port the port from which the {@link Header} was received
     * (currently ignored)
     * @param path the path from which the {@link Header} was received
     * @param setCookie the Set-Cookie {@link Header} received from the
     * server
     * @return an array of Cookies parsed from the Set-Cookie {@link
     * Header}
     * @throws HttpException if an exception occurs during parsing
     * @throws IllegalArgumentException if domain or path are null
     * 
     * @deprecated use {@link CookieSpec} interface
     */
    public static Cookie[] parse(
        String domain, int port, String path, Header setCookie) 
        throws HttpException, IllegalArgumentException {
            
        LOG.trace("enter Cookie.parse(String, int, String, Header)");
        return Cookie.parse(domain, port, path, false, setCookie);
    }

    /**
     * Parses the Set-Cookie {@link Header} into an array of
     * Cookies, assuming that the cookies were recieved
     * on an insecure channel.
     *
     * @param domain the domain from which the {@link Header} was received
     * @param path the path from which the {@link Header} was received
     * @param setCookie the Set-Cookie {@link Header} received from the
     * server
     * @return an array of Cookies parsed from the Set-Cookie {@link
     * Header}
     * @throws HttpException if an exception occurs during parsing
     * @throws IllegalArgumentException if domain or path are null
     * 
     * @deprecated use {@link CookieSpec} interface
     */
    public static Cookie[] parse(String domain, String path, Header setCookie) 
    throws HttpException, IllegalArgumentException {
        LOG.trace("enter Cookie.parse(String, String, Header)");
        return Cookie.parse (domain, 80, path, false, setCookie);
    }

    /**
     * Parses the Set-Cookie {@link Header} into an array of
     * Cookies.
     *
     * @param domain the domain from which the {@link Header} was received
     * @param path the path from which the {@link Header} was received
     * @param secure true when the header was recieved over a secure
     * channel
     * @param setCookie the Set-Cookie {@link Header} received from the
     * server
     * @return an array of Cookies parsed from the Set-Cookie {@link
     * Header}
     * @throws HttpException if an exception occurs during parsing
     * @throws IllegalArgumentException if domain or path are null
     * 
     * @deprecated use {@link CookieSpec} interface
     */
    public static Cookie[] parse(String domain, String path, 
        boolean secure, Header setCookie) 
        throws HttpException, IllegalArgumentException {
            
        LOG.trace ("enter Cookie.parse(String, String, boolean, Header)");
        return Cookie.parse (
            domain, (secure ? 443 : 80), path, secure, setCookie);
    }

    /**
      * Parses the Set-Cookie {@link Header} into an array of
      * Cookies.
      *
      * 
The syntax for the Set-Cookie response header is:
      *
      * 

      * set-cookie      =    "Set-Cookie:" cookies
      * cookies         =    1#cookie
      * cookie          =    NAME "=" VALUE * (";" cookie-av)
      * NAME            =    attr
      * VALUE           =    value
      * cookie-av       =    "Comment" "=" value
      *                 |    "Domain" "=" value
      *                 |    "Max-Age" "=" value
      *                 |    "Path" "=" value
      *                 |    "Secure"
      *                 |    "Version" "=" 1*DIGIT
      * 

      *
      * @param domain the domain from which the {@link Header} was received
      * @param port The port from which the {@link Header} was received.
      * @param path the path from which the {@link Header} was received
      * @param secure true when the {@link Header} was received over
      * HTTPS
      * @param setCookie the Set-Cookie {@link Header} received from
      * the server
      * @return an array of Cookies parsed from the Set-Cookie {@link
      * Header}
      * @throws HttpException if an exception occurs during parsing
      * 
      * @deprecated use {@link CookieSpec} interface
      */
    public static Cookie[] parse(String domain, int port, String path, 
        boolean secure, Header setCookie) 
        throws HttpException {
            
        LOG.trace("enter Cookie.parse(String, int, String, boolean, Header)");

        CookieSpec parser = CookiePolicy.getDefaultSpec();
        Cookie[] cookies = parser.parse(domain, port, path, secure, setCookie);

        for (int i = 0; i < cookies.length; i++) {
            final Cookie cookie = cookies[i];
            final CookieSpec validator 
                = CookiePolicy.getSpecByVersion(cookie.getVersion());
            validator.validate(domain, port, path, secure, cookie);
        }
        return cookies;
    }

   // ----------------------------------------------------- Instance Variables

   /** Comment attribute. */
   private String  cookieComment;

   /** Domain attribute. */
   private String  cookieDomain;

   /** Expiration {@link Date}. */
   private Date    cookieExpiryDate;

   /** Path attribute. */
   private String  cookiePath;

   /** My secure flag. */
   private boolean isSecure;

   /**
    * Specifies if the set-cookie header included a Path attribute for this
    * cookie
    */
   private boolean hasPathAttribute = false;

   /**
    * Specifies if the set-cookie header included a Domain attribute for this
    * cookie
    */
   private boolean hasDomainAttribute = false;

   /** The version of the cookie specification I was created from. */
   private int     cookieVersion = 0;

   // -------------------------------------------------------------- Constants

   /** 
    * Collator for Cookie comparisons.  Could be replaced with references to
    * specific Locales.
    */
   private static final RuleBasedCollator STRING_COLLATOR =
        (RuleBasedCollator) RuleBasedCollator.getInstance(
                                                new Locale("en", "US", ""));

   /** Log object for this class */
   private static final Log LOG = LogFactory.getLog(Cookie.class);

}



my book on functional programming
 
new blog posts
Would you hire yourself?
Self-administering intravenous medicine with a PICC Line
I Still Forgive You

Scala: What do 'effect' and 'effectful' mean in functional programming?
Independence Mine railroad in Hatcher Pass, Alaska
'The Man in the Arena' speech by Teddy Roosevelt

This I Love, Guns N' Roses
Scala Book: Free PDF, Mobi, and ePub versions
A new Scala Book cover


 

Copyright 1998-2019 Alvin Alexander, alvinalexander.com
All Rights Reserved.

A percentage of advertising revenue from
pages under the /java/jwarehouse URI on this website is
paid back to open source projects.

 